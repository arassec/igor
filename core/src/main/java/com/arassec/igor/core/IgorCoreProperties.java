package com.arassec.igor.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Core properties to configure igor.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "igor.core")
public class IgorCoreProperties {

    /**
     * Maximum number of parallel jobs.
     */
    private int jobQueueSize = 5;

}
