package com.cadence;

import org.springframework.boot.SpringApplication;

/** Runs the app locally against a throwaway PostgreSQL, without docker compose. */
public final class TestCadenceApplication {

    private TestCadenceApplication() {}

    public static void main(String[] args) {
        SpringApplication.from(CadenceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
