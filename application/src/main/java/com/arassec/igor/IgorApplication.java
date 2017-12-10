package com.arassec.igor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Starts the igor application.
 * <p>
 * Created by sensen on 3/26/17.
 */
@SpringBootApplication
@EnableScheduling
public class IgorApplication {

    /**
     * Starts the igor application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(IgorApplication.class, args);
    }

}
