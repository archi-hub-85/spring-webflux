package ru.akh.spring_webflux.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import ru.akh.spring_webflux.access.SecuredReader;
import ru.akh.spring_webflux.access.SecuredWriter;
import ru.akh.spring_webflux.dao.BookRepository;
import ru.akh.spring_webflux.dao.exception.BookException;
import ru.akh.spring_webflux.dto.BookContent;

@RestController
@RequestMapping("/books")
public class BookContentController {

    @Autowired
    private BookRepository repository;

    @PostMapping(path = "/upload")
    @SecuredWriter
    public Mono<Void> upload(@RequestPart("id") Long id, @RequestPart("file") Mono<FilePart> fileMono) {
        return fileMono
                .flatMap(file -> {
                    long size = file.headers().getContentLength();
                    if (size == 0) {
                        throw new BookException("Empty file!");
                    }

                    BookContent bookContent = new BookContent();
                    bookContent.setId(id);
                    bookContent.setFileName(getFileName(file.filename()));
                    bookContent.setMimeType(file.headers().getContentType().toString());
                    bookContent.setContent(DataBufferUtils.join(file.content()).block().asByteBuffer().array());
                    bookContent.setSize(size);

                    return repository.putContent(bookContent);
                });
    }

    @GetMapping("/download/{id}")
    @SecuredReader
    public Mono<ResponseEntity<Resource>> download(@PathVariable long id) {
        return repository.getContent(id)
                .map(bookContent -> {
                    try (InputStream content = new ByteArrayInputStream(bookContent.getContent())) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set(HttpHeaders.CONTENT_TYPE, bookContent.getMimeType());
                        headers.setContentDisposition(
                                ContentDisposition.attachment().filename(bookContent.getFileName()).build());
                        headers.setContentLength(bookContent.getSize());

                        return new ResponseEntity<Resource>(new InputStreamResource(content), headers, HttpStatus.OK);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    // org.springframework.web.multipart.commons.CommonsMultipartFile#getOriginalFilename()
    private static String getFileName(String path) {
        int unixSep = path.lastIndexOf('/');
        int winSep = path.lastIndexOf('\\');
        int pos = Math.max(winSep, unixSep);

        return (pos >= 0) ? path.substring(pos + 1) : path;
    }

}
