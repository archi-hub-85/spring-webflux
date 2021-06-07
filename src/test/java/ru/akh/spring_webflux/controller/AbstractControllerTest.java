package ru.akh.spring_webflux.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource("classpath:/application-test.properties")
abstract class AbstractControllerTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected WebTestClient client;

    private long testStartTime;

    @BeforeEach
    void setUp(ApplicationContext context) {
        client = WebTestClient.bindToApplicationContext(context)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .build()
                .mutateWith(SecurityMockServerConfigurers.csrf());
    }

    @BeforeEach
    void beforeTest(TestInfo testInfo) {
        logger.debug("Starting test {}...", testInfo.getDisplayName());
        testStartTime = System.currentTimeMillis();

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

    @AfterEach
    void afterTest(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        logger.debug("Test {} took {} ms.", testInfo.getDisplayName(), duration);
    }

}
