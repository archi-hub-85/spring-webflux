package ru.akh.spring_webflux.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithUser(username = UsersConstants.ADMIN_USERNAME, password = UsersConstants.ADMIN_PASSWORD)
public @interface WithAdmin {

}
