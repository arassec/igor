package com.arassec.igor.persistence;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.persistence.mapper.IgorComponentPersistenceDeserializer;
import com.arassec.igor.persistence.mapper.IgorComponentPersistenceSerializer;
import com.arassec.igor.persistence.security.SecurityProvider;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;

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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));

        mapperModule.addDeserializer(Service.class, new IgorComponentPersistenceDeserializer<>(Service.class,
                igorComponentRegistry, serviceRepository, securityProvider));
        mapperModule.addDeserializer(Action.class, new IgorComponentPersistenceDeserializer<>(Action.class,
                igorComponentRegistry, serviceRepository, securityProvider));
        mapperModule.addDeserializer(Trigger.class, new IgorComponentPersistenceDeserializer<>(Trigger.class,
                igorComponentRegistry, serviceRepository, securityProvider));
        mapperModule.addDeserializer(Provider.class, new IgorComponentPersistenceDeserializer<>(Provider.class,
                igorComponentRegistry, serviceRepository, securityProvider));

        mapperModule.addSerializer(Instant.class, new StdSerializer<>(Instant.class) {
            @Override
            public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeNumber(instant.toEpochMilli());
            }
        });
        mapperModule.addDeserializer(Instant.class, new StdDeserializer<>(Instant.class) {
            @Override
            public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return Instant.ofEpochMilli(jsonParser.readValueAs(Long.class));
            }
        });

        objectMapper.registerModule(mapperModule);

        return objectMapper;
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentPersistenceSerializer(securityProvider));
        mapperModule.addDeserializer(Service.class, new IgorComponentPersistenceDeserializer<>(Service.class,
                igorComponentRegistry, null, securityProvider));

        objectMapper.registerModule(mapperModule);

        return objectMapper;
    }

}
