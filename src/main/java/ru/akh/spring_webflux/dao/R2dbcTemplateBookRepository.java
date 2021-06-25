package ru.akh.spring_webflux.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dao.exception.BookContentNotFoundException;
import ru.akh.spring_webflux.dao.exception.BookNotFoundException;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

@Repository("bookRepository")
@Profile("r2dbc_template")
public class R2dbcTemplateBookRepository extends AbstractR2dbcBookRepository {

    @Override
    public Mono<Book> get(long id) {
        return template.select(Book.class).from("BOOKS_WITH_AUTHORS").matching(Query.query(Criteria.where("id").is(id)))
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
            fieldName = field.toString();
            break;
        case AUTHOR:
            fieldName = "NAME";
            break;
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }

        return template.select(Book.class).from("BOOKS_WITH_AUTHORS")
                .matching(Query.query(Criteria.empty()).sort(Sort.by(fieldName)).limit(limit)).all();
    }

    @Override
    public Flux<Book> getBooksByAuthor(String author) {
        return template.select(Book.class).from("BOOKS_WITH_AUTHORS")
                .matching(Query.query(Criteria.where("NAME").is(author))).all();
    }

    @Override
    public Mono<BookContent> getContent(long id) {
        return template.select(BookContent.class).from("BOOKS_WITH_CONTENT")
                .matching(Query.query(Criteria.where("id").is(id))).one()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BookContentNotFoundException(id))));
    }

}
