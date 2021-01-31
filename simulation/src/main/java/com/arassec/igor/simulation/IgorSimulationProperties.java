package com.arassec.igor.simulation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the simulation module.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "igor.simulation")
public class IgorSimulationProperties {

    /**
     * Timeout in seconds until a simulated job execution will be cancelled.
     */
    private Long timeout = 900L;

}
