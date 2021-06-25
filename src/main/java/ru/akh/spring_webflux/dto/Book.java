package ru.akh.spring_webflux.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

public class Book {

    public static enum Field {
        ID,
        TITLE,
        YEAR,
        AUTHOR
    }

    @Id
    private Long id;

    @NotBlank
    private String title;

    private int year;

    @NotNull
    @Valid
    private Author author;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", year=" + year + ", author=" + author + "]";
    }

}
