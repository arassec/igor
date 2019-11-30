package com.arassec.igor.persistence;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.mapper.*;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the persistence layer for igor.
 */
@Configuration
public class PersistenceConfiguration {

    /**
     * Creates an {@link ObjectMapper} for igor {@link Job}s.
     *
     * @param igorComponentRegistry The igor component registry.
     * @param serviceRepository     The repository for services.
     * @param securityProvider      The security provider for en- and decrypting secured parameter values.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    @Bean(name = "persistenceJobMapper")
    public ObjectMapper persistenceJobMapper(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository,
                                             SecurityProvider securityProvider) {

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));

        mapperModule.addDeserializer(Service.class, new ServicePersistenceDeserializer(igorComponentRegistry, securityProvider));
        mapperModule.addDeserializer(Action.class, new ActionPersistenceDeserializer(igorComponentRegistry, serviceRepository, securityProvider));
        mapperModule.addDeserializer(Trigger.class, new TriggerPersistenceDeserializer(igorComponentRegistry, serviceRepository, securityProvider));
        mapperModule.addDeserializer(Provider.class, new ProviderPersistenceDeserializer(igorComponentRegistry, serviceRepository, securityProvider));

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .registerModule(new JavaTimeModule())
                .registerModule(mapperModule);
    }

    /**
     * Creates an {@link ObjectMapper} for igor {@link Service}s. A special mapper is required for services to avoid circular
     * dependencies with the {@link ServiceRepository}, which is normally needed for parameter processing.
     *
     * @param igorComponentRegistry The igor component registry.
     * @param securityProvider      The security provider for en- and decrypting secured parameter values.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    @Bean(name = "persistenceServiceMapper")
    public ObjectMapper persistenceServiceMapper(IgorComponentRegistry igorComponentRegistry, SecurityProvider securityProvider) {

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));
        mapperModule.addDeserializer(Service.class, new ServicePersistenceDeserializer(igorComponentRegistry, securityProvider));

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .registerModule(mapperModule);
    }

}
