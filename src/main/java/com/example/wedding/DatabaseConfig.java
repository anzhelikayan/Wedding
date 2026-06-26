package com.example.wedding;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {

    @Bean
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource railwayDataSource(
            @Value("${DATABASE_URL}") String databaseUrl,
            @Value("${SPRING_DATASOURCE_USERNAME:}") String fallbackUsername,
            @Value("${SPRING_DATASOURCE_PASSWORD:}") String fallbackPassword
    ) {
        HikariDataSource dataSource = new HikariDataSource();

        if (databaseUrl.startsWith("jdbc:")) {
            dataSource.setJdbcUrl(databaseUrl);
            dataSource.setUsername(fallbackUsername);
            dataSource.setPassword(fallbackPassword);
            return dataSource;
        }

        URI uri = URI.create(databaseUrl);
        String userInfo = uri.getUserInfo() == null ? "" : uri.getUserInfo();
        String[] credentials = userInfo.split(":", 2);
        String query = uri.getQuery() == null || uri.getQuery().isBlank() ? "" : "?" + uri.getQuery();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() == -1 ? "" : ":" + uri.getPort())
                + uri.getPath()
                + query;

        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(credentials.length > 0 ? decode(credentials[0]) : fallbackUsername);
        dataSource.setPassword(credentials.length > 1 ? decode(credentials[1]) : fallbackPassword);
        return dataSource;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
