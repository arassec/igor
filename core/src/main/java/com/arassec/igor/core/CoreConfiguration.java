package com.arassec.igor.core;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Igor's core configuration.
 */
@Configuration
@ComponentScan
@EnableScheduling
@EnableConfigurationProperties(IgorCoreProperties.class)
public class CoreConfiguration {

    /**
     * Creates a new {@link TaskScheduler} for igor's jobs.
     *
     * @return A newly created {@link TaskScheduler}.
     */
    @Bean
    public TaskScheduler jobScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("jobScheduler");
        scheduler.setPoolSize(10);
        return scheduler;
    }

}
