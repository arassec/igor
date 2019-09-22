package com.arassec.igor.web;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.web.mapper.IgorComponentWebDeserializer;
import com.arassec.igor.web.mapper.IgorComponentWebSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;

/**
 * Configures the web layer of igor.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfiguration {

    /**
     * The repository for {@link Service}s.
     */
    private final ServiceRepository serviceRepository;

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Creates the default object mapper for the web layer.
     *
     * @return The default {@link ObjectMapper}.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return createObjectMapper(false);
    }

    /**
     * Creates an {@link ObjectMapper} with enabled "simulation mode" to support simulated job executions.
     *
     * @return The {@link ObjectMapper} for simulated job runs.
     */
    @Bean
    public ObjectMapper simulationObjectMapper() {
        return createObjectMapper(true);
    }

    /**
     * Creates an {@link ObjectMapper} with optional simulation mode enabled.
     *
     * @param simulationMode Set to {@code true} to support simulated job runs, {@code false} otherwise.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    private ObjectMapper createObjectMapper(boolean simulationMode) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(Trigger.class, new IgorComponentWebSerializer<>(Trigger.class, igorComponentRegistry));
        mapperModule.addSerializer(Provider.class, new IgorComponentWebSerializer<>(Provider.class, igorComponentRegistry));
        mapperModule.addSerializer(Action.class, new IgorComponentWebSerializer<>(Action.class, igorComponentRegistry));
        mapperModule.addSerializer(Service.class, new IgorComponentWebSerializer<>(Service.class, igorComponentRegistry));

        mapperModule.addDeserializer(Trigger.class, new IgorComponentWebDeserializer<>(Trigger.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Provider.class, new IgorComponentWebDeserializer<>(Provider.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Action.class, new IgorComponentWebDeserializer<>(Action.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Service.class, new IgorComponentWebDeserializer<>(Service.class,
                serviceRepository, simulationMode));

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

}
