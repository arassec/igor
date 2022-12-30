package com.arassec.igor.web;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.web.mapper.ActionWebDeserializer;
import com.arassec.igor.web.mapper.ConnectorWebDeserializer;
import com.arassec.igor.web.mapper.IgorComponentWebSerializer;
import com.arassec.igor.web.mapper.TriggerWebDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;

/**
 * Configures the web layer of igor.
 */
@Configuration
@ComponentScan
public class WebConfiguration {

    /**
     * Creates a {@link MessageSource} to support I18N. All configured message sources from igor submodules are configured as
     * hierarchy of parent message sources, to allow individual message sources per module.
     *
     * @param messageSources All available message sources.
     * @return The primary message source.
     */
    @Bean
    public MessageSource messageSource(final List<MessageSource> messageSources) {
        var messageSource = new ResourceBundleMessageSource();
        if (messageSources != null && !messageSources.isEmpty()) {
            for (var i = 1; i < messageSources.size(); i++) {
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
     * @param igorComponentRegistry Igor's component registry.
     * @param connectorRepository   Repository for connectors.
     * @param messageSource         Spring's message source for i18n.
     * @param igorComponentUtil     Igor's component util.
     * @return The default {@link ObjectMapper}.
     */
    @Bean
    public ObjectMapper objectMapper(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository,
                                     IgorComponentUtil igorComponentUtil, MessageSource messageSource) {
        var mapperModule = new SimpleModule();

        mapperModule.addSerializer(new IgorComponentWebSerializer(messageSource, igorComponentRegistry, igorComponentUtil));
        mapperModule.addDeserializer(Connector.class, new ConnectorWebDeserializer(igorComponentRegistry));
        mapperModule.addDeserializer(Action.class, new ActionWebDeserializer(igorComponentRegistry, connectorRepository));
        mapperModule.addDeserializer(Trigger.class, new TriggerWebDeserializer(igorComponentRegistry, connectorRepository));

        return JsonMapper.builder()
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(new JavaTimeModule())
            .addModule(mapperModule)
            .build();
    }

}
