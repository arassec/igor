package com.arassec.igor.web;

import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link WebConfiguration}.
 */
@DisplayName("Tests the web layer's configuration.")
class WebConfigurationTest {

    /**
     * The configuration under test.
     */
    private final WebConfiguration webConfiguration = new WebConfiguration();

    /**
     * Tests message-source creation.
     */
    @Test
    @DisplayName("Tests message-source creation.")
    @SneakyThrows
    void testMessageSourceCreation() {
        ResourcePatternResolver resourcePatternResolverMock = mock(ResourcePatternResolver.class);
        when(resourcePatternResolverMock.getResources("classpath*:i18n/*.properties")).thenReturn(new Resource[]{
            new FileSystemResource("/path/to/i18n/first.properties"),
            new FileSystemResource("/another/path/to/i18n/second.properties")
        });

        MessageSource messageSource = webConfiguration.messageSource(resourcePatternResolverMock);

        assertEquals("alpha", messageSource.getMessage("keyA", null, Locale.ROOT));
        assertEquals("beta", messageSource.getMessage("keyB", null, Locale.ROOT));

        assertEquals("keyC", messageSource.getMessage("keyC", null, Locale.ROOT));

        assertEquals("alpha_de", messageSource.getMessage("keyA", null, Locale.GERMAN));
        assertEquals("alpha_de", messageSource.getMessage("keyA", null, Locale.GERMANY));
        assertEquals("beta", messageSource.getMessage("keyB", null, Locale.GERMAN));

        assertEquals("unknown", messageSource.getMessage("unknown", null, Locale.GERMAN));
    }

    /**
     * Tests getting the web layer's object-mapper.
     */
    @Test
    @DisplayName("Tests getting the web layer's object-mappers.")
    void testObjectMapperCreation() {
        IgorComponentUtil igorComponentUtil = mock(IgorComponentUtil.class);
        IgorComponentRegistry igorComponentRegistryMock = mock(IgorComponentRegistry.class);
        ConnectorRepository connectorRepositoryMock = mock(ConnectorRepository.class);
        MessageSource messageSourceMock = mock(MessageSource.class);

        ObjectMapper objectMapper = webConfiguration.objectMapper(igorComponentRegistryMock, connectorRepositoryMock,
            igorComponentUtil, messageSourceMock);

        assertFalse(objectMapper.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(objectMapper.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS));
        assertTrue(objectMapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"));
    }

}
