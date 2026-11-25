package com.cadence;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CadenceApplication {

    // Only main is static. Spring still subclasses this @Configuration class, so the constructor
    // cannot be private; protected keeps "new CadenceApplication()" out of other code.
    protected CadenceApplication() {}

    public static void main(String[] args) {
        // SRS C-09: the app stores and computes in UTC, whatever the machine's time zone is;
        // Vietnam time is applied only through BusinessTime. The PostgreSQL driver also sends the
        // JVM time zone to the server on connect.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(CadenceApplication.class, args);
    }
}
