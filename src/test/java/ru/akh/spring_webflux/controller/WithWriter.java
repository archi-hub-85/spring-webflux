package ru.akh.spring_webflux.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithUser(username = UsersConstants.WRITER_USERNAME, password = UsersConstants.WRITER_PASSWORD)
public @interface WithWriter {

}
