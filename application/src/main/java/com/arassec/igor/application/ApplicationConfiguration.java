package com.arassec.igor.application;


import com.arassec.igor.application.util.IgorConfigHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Igor's application configuration.
 */
@Configuration
@ComponentScan
@EnableScheduling
@EnableConfigurationProperties(IgorApplicationProperties.class)
public class ApplicationConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n for the core module.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource coreMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/core-messages");
    }

    /**
     * Creates a new {@link TaskScheduler} for igor's jobs.
     *
     * @return A newly created {@link TaskScheduler}.
     */
    @Bean
    public TaskScheduler jobScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("jobScheduler");
        scheduler.setPoolSize(10);
        return scheduler;
    }

}
