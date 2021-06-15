package ru.akh.spring_webflux;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import io.r2dbc.spi.ConnectionFactory;
import ru.akh.spring_webflux.dao.BookNamingStrategy;
import ru.akh.spring_webflux.dao.BookReadConverter;
import ru.akh.spring_webflux.dao.BookWriteConverter;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration // (proxyBeanMethods = false)
    // @EnableWebFluxSecurity
    @EnableReactiveMethodSecurity
    public static class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            return http
                    .authorizeExchange().anyExchange().authenticated().and()
                    .httpBasic().and()
                    // .formLogin()
                    .build();
        }

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
            return Arrays.asList(BookReadConverter.INSTANCE, BookWriteConverter.INSTANCE);
        }

    }

}
