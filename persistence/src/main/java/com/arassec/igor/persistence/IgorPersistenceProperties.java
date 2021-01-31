package com.arassec.igor.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the persistence module.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "igor.persistence")
public class IgorPersistenceProperties {

    /**
     * Security token to use for built-in encryption / decryption.
     */
    private String localSecurityToken;

}
