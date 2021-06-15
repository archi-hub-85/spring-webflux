package ru.akh.spring_webflux.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dao.exception.AuthorNotFoundException;
import ru.akh.spring_webflux.dao.exception.BookNotFoundException;
import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

@Repository("bookRepository")
@Profile("r2dbc_template")
@Transactional(readOnly = true)
public class R2dbcTemplateBookRepository implements BookRepository {

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
        return template.select(Book.class).from("BOOKS_WITH_AUTHORS").matching(Query.query(Criteria.where("id").is(id)))
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    public Mono<Void> putContent(BookContent content) {
        // TODO Auto-generated method stub
        return null;
    }

}
