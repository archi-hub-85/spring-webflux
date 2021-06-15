package ru.akh.spring_webflux.dao;

import org.springframework.data.relational.core.mapping.NamingStrategy;

import ru.akh.spring_webflux.dto.Author;
import ru.akh.spring_webflux.dto.Book;
import ru.akh.spring_webflux.dto.BookContent;

public class BookNamingStrategy implements NamingStrategy {

    @Override
    public String getTableName(Class<?> type) {
        String tableName;
        if (type.equals(Book.class) || type.equals(BookContent.class)) {
            tableName = "BOOKS";
        } else if (type.equals(Author.class)) {
            tableName = "AUTHORS";
        } else {
            tableName = NamingStrategy.super.getTableName(type);
        }

        return tableName;
    }

}
