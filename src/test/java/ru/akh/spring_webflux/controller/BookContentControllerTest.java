package ru.akh.spring_webflux.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;

@ActiveProfiles("r2dbc")
public class BookContentControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testDownload() throws IOException {
        testDownload(1, "dark_tower_1.txt", "The Dark Tower: The Gunslinger");
    }

    @Test
    @WithWriter
    public void testUpload() {
        testUpload(2, "/c:\\test.txt", "test content");
    }

    @Test
    @WithAdmin
    public void testUploadThenDownload() throws IOException {
        long id = 3;
        String fileName = "newTest2.txt";
        String content = "new test content #2";

        testUpload(id, fileName, content);
        testDownload(id, fileName, content);
    }

    @Test
    @WithReader
    public void testDownloadForNonExistingBook() {
        expectError(getDownloadRequest(100));
    }

    @Test
    @WithWriter
    public void testUploadForNonExistingBook() {
        expectError(postUploadRequest(100, "test.txt", "test content"));
    }

    @Test
    @WithWriter
    public void testUploadEmptyFile() {
        expectError(postUploadRequest(1, "test.txt", ""));
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testDownloadWithWrongUser() {
        getDownloadRequest(1).expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.READER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testDownloadWithWrongPassword() {
        getDownloadRequest(1).expectStatus().isUnauthorized();
    }

    @Test
    @WithWriter
    public void testDownloadWithWrongRole() {
        getDownloadRequest(1).expectStatus().isForbidden();
    }

    @Test
    @WithUser(username = UsersConstants.WRONG_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testUploadWithWrongUser() {
        postUploadRequest(1, "test.txt", "test content").expectStatus().isUnauthorized();
    }

    @Test
    @WithUser(username = UsersConstants.WRITER_USERNAME, password = UsersConstants.WRONG_PASSWORD)
    public void testUploadWithWrongPassword() {
        postUploadRequest(1, "test.txt", "test content").expectStatus().isUnauthorized();
    }

    @Test
    @WithReader
    public void testUploadWithWrongRole() {
        postUploadRequest(1, "test.txt", "test content").expectStatus().isForbidden();
    }

    private void testDownload(long id, String expectedFileName, String expectedContent) throws IOException {
        EntityExchangeResult<Resource> result = getDownloadRequest(id)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(Resource.class)
                .returnResult();
        Resource resource = result.getResponseBody();

        String content;
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            content = new String(bytes, StandardCharsets.UTF_8);
        }

        Assertions.assertEquals(expectedFileName, resource.getFilename(), "bookContent.fileName");
        Assertions.assertEquals(expectedContent, content, "bookContent.content");
    }

    private void testUpload(long id, String fileName, String content) {
        postUploadRequest(id, fileName, content)
                .expectStatus().isOk();
    }

    private ResponseSpec getDownloadRequest(long id) {
        return client.get()
                .uri("/books/download/{id}", Collections.singletonMap("id", id))
                .accept(MediaType.ALL)
                .exchange();
    }

    private ResponseSpec postUploadRequest(long id, String fileName, String content) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("id", String.valueOf(id), MediaType.TEXT_PLAIN);
        multipartBodyBuilder
                .part("file", new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)), MediaType.TEXT_PLAIN)
                .headers(headers -> {
                    headers.setContentDispositionFormData("file", fileName);
                });

        return client.post().uri("/books/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
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
