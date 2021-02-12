package com.arassec.igor.standalone;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Starts the igor application.
 */
@SpringBootApplication
public class IgorStandaloneApplication {

    /**
     * Starts the igor application.
     *
     * @param args Command-line arguments.
     */
    @SuppressWarnings("squid:S4823") // Spring will take care of command line parameters...
    public static void main(String[] args) {
        SpringApplication.run(IgorStandaloneApplication.class, args);
    }

    /**
     * Creates Open-API information.
     *
     * @param appDescription Description of the application from the module's pom.xml file.
     * @param appVersion     Version of the application from the module's pom.xml file.
     *
     * @return An {@link OpenAPI} specification.
     */
    @Bean
    public OpenAPI customOpenAPI(@Value("${application-description}") String appDescription, @Value("${application-version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("Igor Application API")
                        .version(appVersion)
                        .description(appDescription)
                        .license(new License().name("MIT").url("https://github.com/arassec/igor/blob/master/LICENSE")));
    }

}
