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
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Configures the web layer of igor.
 */
@Configuration
@EnableSwagger2
public class WebConfiguration {

    /**
     * Swagger configuration.
     *
     * @return {@link Docket} for swagger API documentation.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.arassec.igor.web.controller"))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    /**
     * Creates a {@link AcceptHeaderLocaleResolver} that processes the accept-language header and has the ROOT locale configured
     * as fallback.
     *
     * @return A new {@lin LocaleResolver} instance.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ROOT);
        return localeResolver;
    }

    /**
     * Creates a {@link MessageSource} to support I18N. All configured message sources from igor sub-modules are configured as
     * hierarchy of parent message sources, to allow individual message sources per module.
     *
     * @param messageSources All available message sources.
     *
     * @return The primary message source.
     */
    @Bean
    public MessageSource messageSource(final List<MessageSource> messageSources) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        for (int i = 1; i < messageSources.size(); i++) {
            ((HierarchicalMessageSource) messageSources.get(i)).setParentMessageSource(messageSources.get(i - 1));
        }
        messageSource.setParentMessageSource(messageSources.get(messageSources.size() - 1));
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    /**
     * Creates the default object mapper for the web layer.
     *
     * @return The default {@link ObjectMapper}.
     */
    @Bean
    public ObjectMapper objectMapper(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository,
                                     MessageSource messageSource) {
        return createObjectMapper(igorComponentRegistry, serviceRepository, messageSource, false);
    }

    /**
     * Creates an {@link ObjectMapper} with enabled "simulation mode" to support simulated job executions.
     *
     * @return The {@link ObjectMapper} for simulated job runs.
     */
    @Bean
    public ObjectMapper simulationObjectMapper(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository, MessageSource messageSource) {
        return createObjectMapper(igorComponentRegistry, serviceRepository, messageSource, true);
    }

    /**
     * Creates an {@link ObjectMapper} with optional simulation mode enabled.
     *
     * @param simulationMode Set to {@code true} to support simulated job runs, {@code false} otherwise.
     *
     * @return A newly created {@link ObjectMapper} instance.
     */
    private ObjectMapper createObjectMapper(IgorComponentRegistry igorComponentRegistry, ServiceRepository serviceRepository,
                                            MessageSource messageSource, boolean simulationMode) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentWebSerializer(messageSource));
        mapperModule.addDeserializer(Service.class, new IgorComponentWebDeserializer<>(Service.class, igorComponentRegistry,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Action.class, new IgorComponentWebDeserializer<>(Action.class, igorComponentRegistry,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Trigger.class, new IgorComponentWebDeserializer<>(Trigger.class, igorComponentRegistry,
                serviceRepository, simulationMode));
        mapperModule.addDeserializer(Provider.class, new IgorComponentWebDeserializer<>(Provider.class, igorComponentRegistry,
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
