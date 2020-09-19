package com.arassec.igor.persistence;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.persistence.mapper.*;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures the persistence layer for igor.
 */
@Configuration
@ComponentScan
@EnableJpaRepositories
@EntityScan
@EnableTransactionManagement
@EnableConfigurationProperties(IgorPersistenceProperties.class)
public class PersistenceConfiguration {

    /**
     * Creates an {@link ObjectMapper} for igor {@link Job}s.
     *
     * @param igorComponentRegistry The igor component registry.
     * @param connectorRepository   The repository for connectors.
     * @param securityProvider      The security provider for en- and decrypting secured parameter values.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    @Bean(name = "persistenceJobMapper")
    public ObjectMapper persistenceJobMapper(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository,
                                             SecurityProvider securityProvider) {

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));

        mapperModule.addDeserializer(Connector.class, new ConnectorPersistenceDeserializer(igorComponentRegistry, securityProvider));
        mapperModule.addDeserializer(Action.class, new ActionPersistenceDeserializer(igorComponentRegistry, connectorRepository, securityProvider));
        mapperModule.addDeserializer(Trigger.class, new TriggerPersistenceDeserializer(igorComponentRegistry, connectorRepository, securityProvider));

        ObjectMapper result = applyDefaultConfiguration(new ObjectMapper()).registerModule(mapperModule);

        result.addMixIn(Job.class, JobMixin.class);

        return result;
    }

    /**
     * Creates an {@link ObjectMapper} for igor {@link Connector}s. A special mapper is required for connectors to avoid circular
     * dependencies with the {@link ConnectorRepository}, which is normally needed for parameter processing.
     *
     * @param igorComponentRegistry The igor component registry.
     * @param securityProvider      The security provider for en- and decrypting secured parameter values.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    @Bean(name = "persistenceConnectorMapper")
    public ObjectMapper persistenceConnectorMapper(IgorComponentRegistry igorComponentRegistry, SecurityProvider securityProvider) {

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));
        mapperModule.addDeserializer(Connector.class, new ConnectorPersistenceDeserializer(igorComponentRegistry, securityProvider));

        return applyDefaultConfiguration(new ObjectMapper()).registerModule(mapperModule);
    }

    /**
     * Applies the persistence default configuration to the supplied {@link ObjectMapper}.
     *
     * @param objectMapper The object mapper to configure.
     *
     * @return The same object mapper instance with applied configuration settings.
     */
    public ObjectMapper applyDefaultConfiguration(ObjectMapper objectMapper) {
        return objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .registerModule(new JavaTimeModule());
    }

}
