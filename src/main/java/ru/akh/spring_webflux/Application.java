package ru.akh.spring_webflux;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;

import io.r2dbc.spi.ConnectionFactory;
import ru.akh.spring_webflux.controller.LongDecoder;
import ru.akh.spring_webflux.dao.BookNamingStrategy;
import ru.akh.spring_webflux.dao.converter.BookContentReadConverter;
import ru.akh.spring_webflux.dao.converter.BookContentWriteConverter;
import ru.akh.spring_webflux.dao.converter.BookReadConverter;
import ru.akh.spring_webflux.dao.converter.BookWriteConverter;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    public static class MyCodecsConfiguration {

        @Bean
        public CodecCustomizer myCodecCustomizer() {
            return (configurer) -> {
                // configurer.registerDefaults(false);
                configurer.customCodecs().register(new DecoderHttpMessageReader<Long>(new LongDecoder()));
            };
        }

    }

    @Configuration // (proxyBeanMethods = false)
    // @EnableWebFluxSecurity
    @EnableReactiveMethodSecurity
    public static class SecurityConfig {

    }

    @Configuration // (proxyBeanMethods = false)
    @Profile({ "r2dbc", "r2dbc_template" })
    public static class R2dbcConfig extends AbstractR2dbcConfiguration {

        @Bean
        public NamingStrategy namingStrategy() {
            return new BookNamingStrategy();
        }

        // omit @Bean to use Spring Boot's implementation
        @Override
        public ConnectionFactory connectionFactory() {
            return null;
        }

        @Override
        protected List<Object> getCustomConverters() {
            return Arrays.asList(BookReadConverter.INSTANCE, BookWriteConverter.INSTANCE,
                    BookContentReadConverter.INSTANCE, BookContentWriteConverter.INSTANCE);
        }

    }

}
