package com.cadence;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CadenceApplication {

    public static void main(String[] args) {
        // N10: the app always runs in UTC, whatever the machine's time zone is.
        // The PostgreSQL driver sends the JVM time zone to the server on connect.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(CadenceApplication.class, args);
    }

}
