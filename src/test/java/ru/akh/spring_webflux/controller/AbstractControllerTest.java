package ru.akh.spring_webflux.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

import ru.akh.spring_webflux.AbstractTest;
import ru.akh.spring_webflux.dao.BookRepository;

@Import(SecurityConfig.class)
abstract class AbstractControllerTest extends AbstractTest {

    @Autowired
    protected WebTestClient client;

    @MockBean
    protected BookRepository repository;

    @BeforeEach
    void setAuth(TestInfo testInfo) {
        WithUser withUser = AnnotationUtils.findAnnotation(testInfo.getTestMethod().get(), WithUser.class);
        if (withUser == null) {
            withUser = AnnotationUtils.findAnnotation(testInfo.getTestClass().get(), WithUser.class);
        }
        if (withUser != null) {
            String username = withUser.username();
            String password = withUser.password();
            logger.debug("with user: username = {}, password = {}", username, password);
            client = client.mutate()
                    .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                    .build();
        }
    }

}
