package ru.akh.spring_webflux.controller;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.dto.BookContent;

@WebFluxTest(BookContentController.class)
public class BookContentControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testDownload() {
        long id = 1;

        BookContent content = new BookContent();
        content.setId(id);
        content.setFileName("test.txt");
        content.setMimeType(MediaType.TEXT_PLAIN_VALUE);
        content.setContent("test content".getBytes(StandardCharsets.UTF_8));
        Mockito.when(repository.getContent(id)).thenReturn(Mono.just(content));

        downloadRequest(id)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectHeader()
                .contentDisposition(ContentDisposition.attachment().filename(content.getFileName()).build())
                .expectBody().consumeWith(result -> {
                    Assertions.assertArrayEquals(content.getContent(), result.getResponseBody(), "content");
                });
    }

    @Test
    @WithWriter
    public void testUpload() {
        Mockito.when(repository.putContent(Mockito.any())).thenReturn(Mono.empty());

        uploadRequest(2, "/c:\\test.txt", "test content")
                .expectStatus().isOk();

        ArgumentCaptor<BookContent> captor = ArgumentCaptor.forClass(BookContent.class);
        Mockito.verify(repository).putContent(captor.capture());
        BookContent content = captor.getValue();
        Assertions.assertEquals(2, content.getId(), "content.id");
        Assertions.assertEquals("test.txt", content.getFileName(), "content.fileName");
        Assertions.assertEquals(MediaType.TEXT_PLAIN_VALUE, content.getMimeType(), "content.mimeType");
        Assertions.assertArrayEquals("test content".getBytes(StandardCharsets.UTF_8), content.getContent(),
                "content.content");
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testDownloadWithWrongUser() {
        downloadRequest(1)
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.READER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testDownloadWithWrongPassword() {
        downloadRequest(1)
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithWriter
    public void testDownloadWithWrongRole() {
        downloadRequest(1)
                .expectStatus().isForbidden();
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testUploadWithWrongUser() {
        uploadRequest(1, "test.txt", "test content")
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.WRITER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testUploadWithWrongPassword() {
        uploadRequest(1, "test.txt", "test content")
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithReader
    public void testUploadWithWrongRole() {
        uploadRequest(1, "test.txt", "test content")
                .expectStatus().isForbidden();
    }

    private ResponseSpec downloadRequest(long id) {
        return client.get()
                .uri("/books/download/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.ALL)
                .exchange();
    }

    private ResponseSpec uploadRequest(long id, String fileName, String content) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("id", String.valueOf(id), MediaType.TEXT_PLAIN);
        multipartBodyBuilder
                .part("file", new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)), MediaType.TEXT_PLAIN)
                .headers(headers -> {
                    headers.setContentDispositionFormData("file", fileName);
                });

        return client
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/books/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                .exchange();
    }

}
