package ru.akh.spring_webflux.dao.converter;

import org.springframework.core.convert.converter.Converter;

import io.r2dbc.spi.Row;
import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;

public class BookReadConverter implements Converter<Row, Book> {

    public static final BookReadConverter INSTANCE = new BookReadConverter();

    private BookReadConverter() {
    }

    @Override
    public Book convert(Row source) {
        Book book = new Book();
        book.setId(source.get("id", Integer.class).longValue());
        book.setTitle(source.get("title", String.class));
        book.setYear(source.get("year", Integer.class));

        Author author = new Author();
        author.setId(source.get("author_id", Integer.class).longValue());
        author.setName(source.get("name", String.class));
        book.setAuthor(author);

        return book;
    }

}
