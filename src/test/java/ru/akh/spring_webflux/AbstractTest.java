package ru.akh.spring_webflux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private long testStartTime;

    @BeforeEach
    void beforeTest(TestInfo testInfo) {
        logger.debug("Starting test {}...", testInfo.getDisplayName());
        testStartTime = System.currentTimeMillis();
    }

    @AfterEach
    void afterTest(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        logger.debug("Test {} took {} ms.", testInfo.getDisplayName(), duration);
    }

}
