package ru.akh.spring_webflux.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dao.converter.BookContentReadConverter;
import ru.akh.spring_webflux.dao.converter.BookReadConverter;
import ru.akh.spring_webflux.dao.exception.AuthorNotFoundException;
import ru.akh.spring_webflux.dao.exception.BookNotFoundException;
import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

@Repository("bookRepository")
@Profile("r2dbc")
@Transactional(readOnly = true)
public class R2dbcBookRepository implements BookRepository {

    @Autowired
    private DatabaseClient client;

    @Autowired
    private R2dbcEntityTemplate template;

    // This approach doesn't use bean 'r2dbcMappingContext', so you can't customize
    // NamingStrategy!
    /*
     * @Autowired public R2dbcBookRepository(ConnectionFactory connectionFactory) {
     * template = new R2dbcEntityTemplate(connectionFactory); }
     */

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
    @Transactional
    public Mono<Book> put(Book book) {
        Author author = book.getAuthor();
        Long authorId = author.getId();
        Mono<Author> authorMono = (authorId == null) ? template.insert(author)
                : template.update(author).onErrorResume(new RowNotExistPredicate("AUTHORS", authorId),
                        ex -> Mono.error(new AuthorNotFoundException(authorId)));

        return authorMono.flatMap(savedAuthor -> {
            book.setAuthor(savedAuthor);
            Long bookId = book.getId();

            return (bookId == null) ? template.insert(book)
                    : template.update(book).onErrorResume(new RowNotExistPredicate("BOOKS", bookId),
                            ex -> Mono.error(new BookNotFoundException(bookId)));
        });
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

    @Override
    @Transactional
    public Mono<Void> putContent(BookContent content) {
        long bookId = content.getId();
        return template.update(content)
                .onErrorResume(new RowNotExistPredicate("BOOKS", bookId),
                        ex -> Mono.error(new BookNotFoundException(bookId)))
                .then();
    }

}
