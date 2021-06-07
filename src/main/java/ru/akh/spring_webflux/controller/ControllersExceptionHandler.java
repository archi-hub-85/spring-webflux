package ru.akh.spring_webflux.controller;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import ru.akh.spring_webflux.dao.exception.BookException;

@ControllerAdvice("ru.akh.spring_webflux.controller")
public class ControllersExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleWebExchangeBindException(WebExchangeBindException ex) {
        StringBuilder sb = new StringBuilder();
        for (ObjectError error : ex.getAllErrors()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }

            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                sb.append("Field error in object '").append(fieldError.getObjectName()).append("' on field '")
                        .append(fieldError.getField()).append("'");
            } else {
                sb.append("Error in object '").append(error.getObjectName());
            }

            sb.append(": ").append(error.getDefaultMessage());
        }

        HttpHeaders headers = HttpHeaders.writableHttpHeaders(ex.getResponseHeaders());
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity<>(sb.toString(), headers, ex.getStatus());
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<String> handleServerWebInputException(ServerWebInputException ex) {
        HttpHeaders headers = HttpHeaders.writableHttpHeaders(ex.getResponseHeaders());
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity<>(ex.getReason(), headers, ex.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(ConstraintViolationException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookException.class)
    public ResponseEntity<String> handleDaoException(BookException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

}
