package ru.akh.spring_webflux.dao.converter;

import java.nio.ByteBuffer;

import org.springframework.core.convert.converter.Converter;

import io.r2dbc.spi.Row;
import ru.akh.spring_webflux.dto.BookContent;

public class BookContentReadConverter implements Converter<Row, BookContent> {

    public static final BookContentReadConverter INSTANCE = new BookContentReadConverter();

    private BookContentReadConverter() {
    }

    @Override
    public BookContent convert(Row source) {
        BookContent content = new BookContent();
        content.setId(source.get("id", Integer.class).longValue());
        content.setFileName(source.get("filename", String.class));
        content.setMimeType(source.get("mimetype", String.class));
        content.setContent(source.get("content", ByteBuffer.class).array());
        content.setSize(source.get("size", Long.class));

        return content;
    }

}
