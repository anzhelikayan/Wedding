package com.example.wedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseMaintenance implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMaintenance.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMaintenance(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String databaseName = connection.getMetaData().getDatabaseProductName();
            if (!databaseName.toLowerCase().contains("postgresql")) {
                return;
            }
        }

        try {
            jdbcTemplate.execute("alter table guest_response alter column wishes type text using wishes::text");
        } catch (RuntimeException exception) {
            LOGGER.debug("Skipping guest_response.wishes text migration", exception);
        }

        try {
            jdbcTemplate.execute("update guest_response set wishes = null where wishes ~ '^[0-9]+$'");
        } catch (RuntimeException exception) {
            LOGGER.debug("Skipping stale numeric wishes cleanup", exception);
        }
    }
}
