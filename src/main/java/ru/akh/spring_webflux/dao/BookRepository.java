package ru.akh.spring_webflux.dao;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

public interface BookRepository {

    Mono<Book> get(long id);

    Mono<Book> put(@NotNull Book book);

    Flux<Book> getTopBooks(@NotNull Book.Field field, @Min(1) int limit);

    Flux<Book> getBooksByAuthor(@NotNull String author);

    Mono<BookContent> getContent(long id);

    Mono<Void> putContent(@NotNull BookContent content);

}
