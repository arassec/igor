package com.arassec.igor.persistence;


import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link PersistenceConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'Persistence'-configuration tests.")
class PersistenceConfigurationTest {

    /**
     * Mock of the component registry.
     */
    @Mock
    private IgorComponentRegistry igorComponentRegistry;

    /**
     * Mock of the connector repository.
     */
    @Mock
    private ConnectorRepository connectorRepository;

    /**
     * Mock of the security provider.
     */
    @Mock
    private SecurityProvider securityProvider;

    /**
     * Tests the job-mapper configuration.
     */
    @Test
    @DisplayName("Tests the job-mapper configuration.")
    void testJobMapperConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        ObjectMapper jobMapper = persistenceConfiguration.persistenceJobMapper(igorComponentRegistry, connectorRepository, securityProvider);
        assertFalse(jobMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(jobMapper.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertFalse(jobMapper.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertTrue(jobMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"));
    }

    /**
     * Tests the connector-mapper configuration.
     */
    @Test
    @DisplayName("Tests the connector-mapper configuration.")
    void testConnectorMapperConfiguration() {
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        ObjectMapper connectorMapper = persistenceConfiguration.persistenceConnectorMapper(igorComponentRegistry, securityProvider);
        assertFalse(connectorMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertTrue(connectorMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"));
    }

}
