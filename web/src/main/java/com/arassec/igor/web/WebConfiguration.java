package com.arassec.igor.web;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.application.factory.ServiceFactory;
import com.arassec.igor.core.application.factory.TriggerFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;


@Configuration
public class WebConfiguration {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceFactory serviceFactory;

    @Autowired
    private ProviderFactory providerFactory;

    @Autowired
    private TriggerFactory triggerFactory;

    @Autowired
    private ActionFactory actionFactory;

    @Bean
    public ObjectMapper objectMapper() {
        return createObjectMapper(false);
    }

    @Bean
    public ObjectMapper simulationObjectMapper() {
        return createObjectMapper(true);
    }

    private ObjectMapper createObjectMapper(boolean simulationMode) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(Trigger.class, new IgorComponentWebSerializer(Trigger.class, triggerFactory));
        mapperModule.addSerializer(Provider.class, new IgorComponentWebSerializer(Provider.class, providerFactory));
        mapperModule.addSerializer(Action.class, new IgorComponentWebSerializer(Action.class, actionFactory));
        mapperModule.addSerializer(Service.class, new IgorComponentWebSerializer(Service.class, serviceFactory));

        mapperModule.addDeserializer(Trigger.class, new IgorComponentWebDeserializer(Trigger.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Provider.class, new IgorComponentWebDeserializer(Provider.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Action.class, new IgorComponentWebDeserializer(Action.class,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Service.class, new IgorComponentWebDeserializer(Service.class,
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
