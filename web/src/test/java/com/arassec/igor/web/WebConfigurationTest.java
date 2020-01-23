package com.arassec.igor.web;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.IgorConfigHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link WebConfiguration}.
 */
@DisplayName("Tests the web layer's configuration.")
public class WebConfigurationTest {

    /**
     * The configuration under test.
     */
    private WebConfiguration webConfiguration = new WebConfiguration();

    /**
     * Tests creation of a locale-resolver.
     */
    @Test
    @DisplayName("Tests creation of a locale-resolver.")
    void testLocaleResolverCreation() {
        LocaleResolver localeResolver = webConfiguration.localeResolver();
        assertTrue(localeResolver instanceof AcceptHeaderLocaleResolver);
        assertEquals(Locale.ROOT, ((AcceptHeaderLocaleResolver) localeResolver).getDefaultLocale());
    }

    /**
     * Tests message-source creation.
     */
    @Test
    @DisplayName("Tests message-source creation.")
    void testMessageSourceCreation() {
        MessageSource firstMessageSource = IgorConfigHelper.createMessageSource("i18n/first");
        MessageSource secondMessageSource = IgorConfigHelper.createMessageSource("i18n/second");

        MessageSource messageSource = webConfiguration.messageSource(List.of(firstMessageSource, secondMessageSource));

        assertEquals("alpha", messageSource.getMessage("keyA", null, Locale.ROOT));
        assertEquals("beta", messageSource.getMessage("keyB", null, Locale.ROOT));

        assertEquals("keyC", messageSource.getMessage("keyC", null, Locale.ROOT));

        assertEquals("alpha_de", messageSource.getMessage("keyA", null, Locale.GERMAN));
        assertEquals("alpha_de", messageSource.getMessage("keyA", null, Locale.GERMANY));
        assertEquals("beta", messageSource.getMessage("keyB", null, Locale.GERMAN));
    }

    /**
     * Tests getting the web layer's object-mapper.
     */
    @Test
    @DisplayName("Tests getting the web layer's object-mappers.")
    void testObjectMapperCreation() {
        IgorComponentRegistry igorComponentRegistryMock = mock(IgorComponentRegistry.class);
        ServiceRepository serviceRepositoryMock = mock(ServiceRepository.class);
        MessageSource messageSourceMock = mock(MessageSource.class);

        ObjectMapper objectMapper = webConfiguration.objectMapper(igorComponentRegistryMock, serviceRepositoryMock, messageSourceMock);

        assertFalse(objectMapper.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertTrue(objectMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"));

        objectMapper = webConfiguration.simulationObjectMapper(igorComponentRegistryMock, serviceRepositoryMock, messageSourceMock);

        assertFalse(objectMapper.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertTrue(objectMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"));
    }

}
