package ru.akh.spring_webflux.controller;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.r2dbc.spi.ConnectionFactory;
import ru.akh.spring_webflux.interceptor.DebugInterceptor;

@TestConfiguration // (proxyBeanMethods = false)
public class TestConfig {

    @Bean("debugInterceptor")
    public DebugInterceptor debugInterceptor() {
        return new DebugInterceptor();
    }

    @Bean
    public BeanNameAutoProxyCreator getBeanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator bean = new BeanNameAutoProxyCreator();
        bean.setBeanNames("bookRepository");
        bean.setInterceptorNames("debugInterceptor");
        // bean.setProxyTargetClass(true);
        return bean;
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("data.sql")));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }

    @TestConfiguration // (proxyBeanMethods = false)
    // @EnableWebFluxSecurity
    public static class TestSecurityConfig {

        @Bean
        public MapReactiveUserDetailsService userDetailsService() {
            UserDetails reader = User.withUsername(UsersConstants.READER_USERNAME)
                    .password("{noop}" + UsersConstants.READER_PASSWORD)
                    .roles("READER")
                    .build();
            UserDetails writer = User.withUsername(UsersConstants.WRITER_USERNAME)
                    .password("{noop}" + UsersConstants.WRITER_PASSWORD)
                    .roles("WRITER")
                    .build();
            UserDetails admin = User.withUsername(UsersConstants.ADMIN_USERNAME)
                    .password("{noop}" + UsersConstants.ADMIN_PASSWORD)
                    .roles("READER", "WRITER")
                    .build();
            return new MapReactiveUserDetailsService(reader, writer, admin);
        }

    }

}
