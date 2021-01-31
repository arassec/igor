package com.arassec.igor.simulation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for the simulation module.
 */
@Configuration
@ComponentScan
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(IgorSimulationProperties.class)
public class SimulationConfiguration {
}
