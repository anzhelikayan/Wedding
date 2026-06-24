package com.example.wedding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class WeddingApplication {

    public static void main(String[] args) {
        configureDatabaseUrl();
        SpringApplication.run(WeddingApplication.class, args);
    }

    private static void configureDatabaseUrl() {
        if (System.getenv("SPRING_DATASOURCE_URL") != null || System.getenv("DATABASE_URL") == null) {
            return;
        }

        URI databaseUri = URI.create(System.getenv("DATABASE_URL"));
        if (!"postgres".equals(databaseUri.getScheme()) && !"postgresql".equals(databaseUri.getScheme())) {
            return;
        }

        String[] userInfo = databaseUri.getUserInfo() == null
                ? new String[]{"", ""}
                : databaseUri.getUserInfo().split(":", 2);

        String username = decode(userInfo[0]);
        String password = userInfo.length > 1 ? decode(userInfo[1]) : "";
        String jdbcUrl = "jdbc:postgresql://" + databaseUri.getHost() + ":" + databaseUri.getPort() + databaseUri.getPath();

        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", username);
        System.setProperty("spring.datasource.password", password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
