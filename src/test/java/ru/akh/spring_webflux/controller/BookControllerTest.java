package ru.akh.spring_webflux.controller;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;

@WebFluxTest(BookController.class)
public class BookControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testGetBook() {
        long id = 1;

        Book book = createBook(id, "title1", 2021, 2L, "name1");
        Author author = book.getAuthor();
        Mockito.when(repository.get(id)).thenReturn(Mono.just(book));

        getBookRequest(id)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo(book.getTitle())
                .jsonPath("$.year").isEqualTo(book.getYear())
                .jsonPath("$.author.id").isEqualTo(author.getId())
                .jsonPath("$.author.name").isEqualTo(author.getName());
    }

    @Test
    @WithWriter
    public void testPutBook() {
        Book book = createBook(1L, "title1", 2021, 1L, "name1");
        Author author = book.getAuthor();
        Mockito.when(repository.put(Mockito.any())).thenReturn(Mono.just(book));

        putBookRequest(book)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(book.getId())
                .jsonPath("$.title").isEqualTo(book.getTitle())
                .jsonPath("$.year").isEqualTo(book.getYear())
                .jsonPath("$.author.id").isEqualTo(author.getId())
                .jsonPath("$.author.name").isEqualTo(author.getName());
    }

    @Test
    @WithReader
    public void testGetTopBooks() throws Exception {
        Book book1 = createBook(1L, "title1", 2021, 1L, "name1");
        Book book2 = createBook(2L, "title2", 2022, 2L, "name2");
        Mockito.when(repository.getTopBooks(Book.Field.ID, 2)).thenReturn(Flux.just(book1, book2));

        getTopBooksRequest(Book.Field.ID, 2)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(book1.getId())
                .jsonPath("$[0].title").isEqualTo(book1.getTitle())
                .jsonPath("$[0].year").isEqualTo(book1.getYear())
                .jsonPath("$[0].author.id").isEqualTo(book1.getAuthor().getId())
                .jsonPath("$[0].author.name").isEqualTo(book1.getAuthor().getName())
                .jsonPath("$[1].id").isEqualTo(book2.getId())
                .jsonPath("$[1].title").isEqualTo(book2.getTitle())
                .jsonPath("$[1].year").isEqualTo(book2.getYear())
                .jsonPath("$[1].author.id").isEqualTo(book2.getAuthor().getId())
                .jsonPath("$[1].author.name").isEqualTo(book2.getAuthor().getName());
    }

    @Test
    @WithReader
    public void testGetBooksByAuthor() {
        Book book1 = createBook(1L, "title1", 2021, 1L, "name1");
        Book book2 = createBook(2L, "title2", 2022, 1L, "name1");
        Mockito.when(repository.getBooksByAuthor("author")).thenReturn(Flux.just(book1, book2));

        getBooksByAuthorRequest("author")
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(book1.getId())
                .jsonPath("$[0].title").isEqualTo(book1.getTitle())
                .jsonPath("$[0].year").isEqualTo(book1.getYear())
                .jsonPath("$[0].author.id").isEqualTo(book1.getAuthor().getId())
                .jsonPath("$[0].author.name").isEqualTo(book1.getAuthor().getName())
                .jsonPath("$[1].id").isEqualTo(book2.getId())
                .jsonPath("$[1].title").isEqualTo(book2.getTitle())
                .jsonPath("$[1].year").isEqualTo(book2.getYear())
                .jsonPath("$[1].author.id").isEqualTo(book2.getAuthor().getId())
                .jsonPath("$[1].author.name").isEqualTo(book2.getAuthor().getName());
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testGetWithWrongUser() {
        getBookRequest(1)
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.READER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testGetWithWrongPassword() {
        getBookRequest(1)
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithWriter
    public void testGetWithWrongRole() {
        getBookRequest(1)
                .expectStatus().isForbidden();
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testPutWithWrongUser() {
        putBookRequest(1L, "title", 2020, null, "author")
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.WRITER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testPutWithWrongPassword() {
        putBookRequest(1L, "title", 2020, null, "author")
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithReader
    public void testPutWithWrongRole() {
        putBookRequest(1L, "title", 2020, null, "author")
                .expectStatus().isForbidden();
    }

    private ResponseSpec getBookRequest(long id) {
        return client.get()
                .uri("/books/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private ResponseSpec putBookRequest(Long id, String title, int year, Long authorId, String authorName) {
        return putBookRequest(createBook(id, title, year, authorId, authorName));
    }

    private ResponseSpec putBookRequest(Book book) {
        return client
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .put().uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(book)
                .exchange();
    }

    private ResponseSpec getTopBooksRequest(Book.Field field, int limit) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/books/").queryParam("field", field).queryParam("top", limit)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private ResponseSpec getBooksByAuthorRequest(String author) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/books/").queryParam("author", author).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private static Book createBook(Long id, String title, int year, Long authorId, String authorName) {
        Author author = null;
        if (authorId != null || authorName != null) {
            author = new Author();
            author.setId(authorId);
            author.setName(authorName);
        }

        Book book = new Book();
        book.setId(id);
        book.setAuthor(author);
        book.setTitle(title);
        book.setYear(year);

        return book;
    }

}
