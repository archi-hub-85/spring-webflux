package ru.akh.spring_webflux.controller;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.access.SecuredReader;
import ru.akh.spring_webflux.access.SecuredWriter;
import ru.akh.spring_webflux.dao.BookRepository;
import ru.akh.spring_webflux.dto.Book;

@RestController
@Validated
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository repository;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredReader
    public Mono<Book> getBook(@PathVariable long id) {
        return repository.get(id);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredWriter
    public Mono<Book> putBook(@RequestBody @Valid Book book) {
        return repository.put(book);
    }

    @GetMapping(path = "/", params = { "field", "top" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredReader
    public Flux<Book> getTopBooks(@RequestParam("field") @NotNull Book.Field field,
            @RequestParam("top") @Min(1) int limit) {
        return repository.getTopBooks(field, limit);
    }

    @GetMapping(path = "/", params = "author", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredReader
    public Flux<Book> getBooksByAuthor(@RequestParam("author") @NotNull String author) {
        return repository.getBooksByAuthor(author);
    }

}
