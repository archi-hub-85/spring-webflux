package ru.akh.spring_webflux.dao;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.TransientDataAccessResourceException;

/**
 * @see org.springframework.data.r2dbc.core.R2dbcEntityTemplate#formatTransientEntityExceptionMessage
 */
class RowNotExistPredicate implements Predicate<Throwable> {

    private static final Pattern MESSAGE_PATTERN = Pattern
            .compile("Failed to update table \\[(\\w+)]\\. Row with Id \\[([\\w\\-\\{\\}]+)] does not exist\\.");

    private final String tableName;
    private final Object rowId;

    public RowNotExistPredicate(String tableName, Object rowId) {
        this.tableName = tableName;
        this.rowId = rowId;
    }

    @Override
    public boolean test(Throwable ex) {
        if (!(ex instanceof TransientDataAccessResourceException)) {
            return false;
        }

        Matcher matcher = MESSAGE_PATTERN.matcher(ex.getMessage());
        return matcher.matches() && matcher.group(1).equalsIgnoreCase(tableName)
                && matcher.group(2).equals(rowId.toString());
    }

}
