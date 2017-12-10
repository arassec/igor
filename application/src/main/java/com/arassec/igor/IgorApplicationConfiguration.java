package com.arassec.igor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Created by Andreas Sensen on 06.05.2017.
 */
@Configuration
public class IgorApplicationConfiguration {

    @Bean
    public TaskScheduler jobScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("jobScheduler");
        scheduler.setPoolSize(10);
        return scheduler;
    }

}
