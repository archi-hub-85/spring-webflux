package ru.akh.spring_webflux.dao;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import ru.akh.spring_webflux.dto.BookContent;

public class BookContentWriteConverter implements Converter<BookContent, OutboundRow> {

    public static final BookContentWriteConverter INSTANCE = new BookContentWriteConverter();

    private BookContentWriteConverter() {
    }

    @Override
    public OutboundRow convert(BookContent source) {
        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.from(source.getId()));
        row.put("filename", Parameter.from(source.getFileName()));
        row.put("mimetype", Parameter.from(source.getMimeType()));
        row.put("content", Parameter.from(source.getContent()));
        return row;
    }

}
