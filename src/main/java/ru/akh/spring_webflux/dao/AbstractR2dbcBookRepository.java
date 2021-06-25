package ru.akh.spring_webflux.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dao.exception.AuthorNotFoundException;
import ru.akh.spring_webflux.dao.exception.BookContentNotFoundException;
import ru.akh.spring_webflux.dao.exception.BookNotFoundException;
import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

@Transactional(readOnly = true)
abstract class AbstractR2dbcBookRepository implements BookRepository {

    @Autowired
    protected R2dbcEntityTemplate template;

    // This approach doesn't use bean 'r2dbcMappingContext', so you can't customize
    // NamingStrategy!
    /*
     * @Autowired public AbstractR2dbcBookRepository(ConnectionFactory
     * connectionFactory) { template = new R2dbcEntityTemplate(connectionFactory); }
     */

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
    @Transactional
    public Mono<Void> putContent(BookContent content) {
        long bookId = content.getId();
        return template.update(content)
                .onErrorResume(new RowNotExistPredicate("BOOKS", bookId),
                        ex -> Mono.error(new BookContentNotFoundException(bookId)))
                .then();
    }

}
