package com.bombazine.setlist_builder;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestcontainersConfig {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("setlist_builder_test")
                .withUsername("postgres")
                .withPassword("postgres");
        POSTGRES.start();
    }
}
