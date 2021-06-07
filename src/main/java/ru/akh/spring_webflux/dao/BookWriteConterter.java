package ru.akh.spring_webflux.dao;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import ru.akh.spring_webflux.dto.Book;

public class BookWriteConterter implements Converter<Book, OutboundRow> {

    @Override
    public OutboundRow convert(Book source) {
        OutboundRow row = new OutboundRow();
        if (source.getId() != null) {
            row.put("id", Parameter.from(source.getId()));
        }
        row.put("title", Parameter.from(source.getTitle()));
        row.put("year", Parameter.from(source.getYear()));
        row.put("author_id", Parameter.from(source.getAuthor().getId()));
        return row;
    }

}
