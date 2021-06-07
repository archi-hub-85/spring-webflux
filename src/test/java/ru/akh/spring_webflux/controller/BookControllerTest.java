package ru.akh.spring_webflux.controller;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;

@ActiveProfiles("r2dbc")
public class BookControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testGetBook() {
        Book book = getBook(1);
        Author author = book.getAuthor();

        Assertions.assertEquals(1, book.getId(), "book.id");
        Assertions.assertEquals("The Dark Tower: The Gunslinger", book.getTitle(), "book.title");
        Assertions.assertEquals(1982, book.getYear(), "book.year");
        Assertions.assertNotNull(author, "book.author");
        Assertions.assertEquals(1, author.getId(), "author.name");
        Assertions.assertEquals("Stephen King", author.getName(), "author.name");
    }

    @Test
    @WithWriter
    public void testPutBook() {
        Book book = putBook(null, "titleNew", 2020, null, "authorNew");
        Assertions.assertTrue(book.getId() > 18, "new book's id must be greater than 18");
    }

    @Test
    @WithAdmin
    public void testUpdateBook() {
        String authorName = "authorNew3";
        String title = "titleNew3";
        int year = 2020;
        Book newBook = putBook(null, title, year, null, authorName);
        long id = newBook.getId();
        Author newAuthor = newBook.getAuthor();

        Assertions.assertEquals(id, newBook.getId(), "newBook.id");
        Assertions.assertEquals(title, newBook.getTitle(), "newBook.title");
        Assertions.assertEquals(year, newBook.getYear(), "newBook.year");
        Assertions.assertNotNull(newAuthor, "newBook.author");
        Assertions.assertTrue(newAuthor.getId() > 3, "newAuthor's id nust be greater than 3");
        Assertions.assertEquals(authorName, newAuthor.getName(), "newAuthor.name");

        String newAuthorName = "authorNew3_2";
        String newTitle = "titleNew3_2";
        int newYear = 2021;
        Book updatedBook = putBook(id, newTitle, newYear, newAuthor.getId(), newAuthorName);
        Author updatedAuthor = updatedBook.getAuthor();

        Assertions.assertEquals(id, updatedBook.getId(), "updatedBook.id");
        Assertions.assertEquals(newTitle, updatedBook.getTitle(), "updatedBook.title");
        Assertions.assertEquals(newYear, updatedBook.getYear(), "updatedBook.year");
        Assertions.assertNotNull(updatedAuthor, "updatedBook.author");
        Assertions.assertEquals(newAuthor.getId(), updatedAuthor.getId(), "updatedAuthor.id");
        Assertions.assertEquals(newAuthorName, updatedAuthor.getName(), "updatedAuthor.name");
    }

    @Test
    @WithReader
    public void testGetNonExistingBook() {
        expectError(getBookRequest(100));
    }

    @Test
    @WithWriter
    public void testPutBookWithNonExistingId() {
        expectError(putBookRequest(100L, "title", 2020, null, "author"));
    }

    @Test
    @WithWriter
    public void testPutBookWithoutTitle() {
        expectError(putBookRequest(1L, null, 2020, null, "author"));
    }

    @Test
    @WithWriter
    public void testPutBookWithoutAuthor() {
        expectError(putBookRequest(1L, "title", 2020, null, null));
    }

    @Test
    @WithWriter
    public void testPutBookWithoutAuthorName() {
        expectError(putBookRequest(1L, "title", 2020, 1L, null));
    }

    @Test
    @WithWriter
    public void testPutBookWithNonExistingAuthorId() {
        expectError(putBookRequest(1L, "title", 2020, 100L, "author"));
    }

    @DisplayName("testGetTopBooks")
    @ParameterizedTest(name = ParameterizedTest.DISPLAY_NAME_PLACEHOLDER + "(" + ParameterizedTest.ARGUMENTS_PLACEHOLDER
            + ")")
    @EnumSource(Book.Field.class)
    @WithReader
    public void testGetTopBooks(Book.Field field) {
        int limit = 5;
        getTopBooksRequest(field, limit)
                .expectStatus().isOk()
                .expectBodyList(Book.class).hasSize(limit);
    }

    @Test
    @WithReader
    public void testGetTopBooksWithNullField() {
        expectError(getTopBooksRequest(null, 5));
    }

    @Test
    @WithReader
    public void testGetTopBooksWithZeroLimit() {
        expectError(getTopBooksRequest(Book.Field.ID, 0));
    }

    @Test
    @WithReader
    public void testGetBooksByAuthor() {
        getBooksByAuthorRequest("Arthur Conan Doyle")
                .expectStatus().isOk()
                .expectBodyList(Book.class).hasSize(4);
    }

    @Test
    @WithReader
    public void testGetBooksByAuthorWithNullAuthor() {
        expectError(getBooksByAuthorRequest(null));
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testGetWithWrongUser() {
        getBookRequest(1).expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.READER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testGetWithWrongPassword() {
        getBookRequest(1).expectStatus().isUnauthorized();
    }

    @Test
    @WithWriter
    public void testGetWithWrongRole() {
        getBookRequest(1).expectStatus().isForbidden();
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testPutWithWrongUser() {
        putBookRequest(1L, "title", 2020, null, "author").expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.WRITER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testPutWithWrongPassword() {
        putBookRequest(1L, "title", 2020, null, "author").expectStatus().isUnauthorized();
    }

    @Test
    @WithReader
    public void testPutWithWrongRole() {
        putBookRequest(1L, "title", 2020, null, "author").expectStatus().isForbidden();
    }

    private Book getBook(long id) {
        EntityExchangeResult<Book> result = getBookRequest(id)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Book.class).returnResult();

        return result.getResponseBody();
    }

    private Book putBook(Long id, String title, int year, Long authorId, String authorName) {
        EntityExchangeResult<Book> result = putBookRequest(id, title, year, authorId, authorName)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Book.class).returnResult();

        return result.getResponseBody();
    }

    private ResponseSpec getBookRequest(long id) {
        return client.get()
                .uri("/books/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    private ResponseSpec putBookRequest(Long id, String title, int year, Long authorId, String authorName) {
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

        return client.put().uri("/books")
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

    private void expectError(ResponseSpec responseSpec) {
        responseSpec
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class).consumeWith(result -> {
                    logger.debug("message = {}", result.getResponseBody());
                });
    }

}
