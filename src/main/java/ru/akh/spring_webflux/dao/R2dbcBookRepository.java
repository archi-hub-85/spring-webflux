package ru.akh.spring_webflux.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dao.converter.BookContentReadConverter;
import ru.akh.spring_webflux.dao.converter.BookReadConverter;
import ru.akh.spring_webflux.dao.exception.BookNotFoundException;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

@Repository("bookRepository")
@Profile("r2dbc")
public class R2dbcBookRepository extends AbstractR2dbcBookRepository {

    @Autowired
    private DatabaseClient client;

    @Override
    public Mono<Book> get(long id) {
        return client.sql(
                "select B.ID, B.TITLE, B.YEAR, B.AUTHOR_ID, A.NAME from BOOKS B inner join AUTHORS A on B.AUTHOR_ID = A.ID where B.ID = :id")
                .bind("id", id)
                .map(BookReadConverter.INSTANCE::convert)
                .one()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BookNotFoundException(id))));
    }

    @Override
    public Flux<Book> getTopBooks(Book.Field field, int limit) {
        String fieldName;
        switch (field) {
        case ID:
        case TITLE:
        case YEAR:
            fieldName = "B." + field.toString();
            break;
        case AUTHOR:
            fieldName = "A.NAME";
            break;
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }

        return client.sql(
                "select B.ID, B.TITLE, B.YEAR, B.AUTHOR_ID, A.NAME from BOOKS B inner join AUTHORS A on B.AUTHOR_ID = A.ID order by "
                        + fieldName + " LIMIT :limit")
                .bind("limit", limit)
                // .filter(statement -> statement.fetchSize(limit))
                .map(BookReadConverter.INSTANCE::convert)
                .all();
    }

    @Override
    public Flux<Book> getBooksByAuthor(String author) {
        return client.sql(
                "select B.ID, B.TITLE, B.YEAR, B.AUTHOR_ID, A.NAME from BOOKS B inner join AUTHORS A on B.AUTHOR_ID = A.ID where A.NAME = :name")
                .bind("name", author)
                .map(BookReadConverter.INSTANCE::convert)
                .all();
    }

    @Override
    public Mono<BookContent> getContent(long id) {
        return client.sql(
                "select ID, FILENAME, MIMETYPE, CONTENT, LENGTH(CONTENT) as \"SIZE\" from BOOKS where ID = :id")
                .bind("id", id)
                .map(BookContentReadConverter.INSTANCE::convert)
                .one()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BookNotFoundException(id))));
    }

}
