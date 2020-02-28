package com.arassec.igor.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the igor application.
 */
@SpringBootApplication
public class IgorApplication {

    /**
     * Starts the igor application.
     *
     * @param args Command-line arguments.
     */
    @SuppressWarnings("squid:S4823") // Spring will take care of command line parameters...
    public static void main(String[] args) {
        SpringApplication.run(IgorApplication.class, args);
    }

}