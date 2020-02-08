package com.arassec.igor.web;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.web.mapper.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configures the web layer of igor.
 */
@Configuration
public class WebConfiguration {

    /**
     * Creates a {@link AcceptHeaderLocaleResolver} that processes the accept-language header and has the ROOT locale configured
     * as fallback.
     *
     * @return A new {@link LocaleResolver} instance.
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
        if (messageSources != null && !messageSources.isEmpty()) {
            for (int i = 1; i < messageSources.size(); i++) {
                ((HierarchicalMessageSource) messageSources.get(i)).setParentMessageSource(messageSources.get(i - 1));
            }
            messageSource.setParentMessageSource(messageSources.get(messageSources.size() - 1));
        }
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

        SimpleModule mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentWebSerializer(messageSource, igorComponentRegistry));
        mapperModule.addDeserializer(Service.class, new ServiceWebDeserializer(igorComponentRegistry, simulationMode));
        mapperModule.addDeserializer(Action.class, new ActionWebDeserializer(igorComponentRegistry, serviceRepository,
                simulationMode));
        mapperModule.addDeserializer(Trigger.class, new TriggerWebDeserializer(igorComponentRegistry, serviceRepository,
                simulationMode));
        mapperModule.addDeserializer(Provider.class, new ProviderWebDeserializer(igorComponentRegistry, serviceRepository,
                simulationMode));

        return new ObjectMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule())
                .registerModule(mapperModule);
    }

}
