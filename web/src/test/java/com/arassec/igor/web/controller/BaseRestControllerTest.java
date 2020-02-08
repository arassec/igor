package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.IgorConfigHelper;
import com.arassec.igor.web.WebConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Base class for Rest-Controller tests.
 */
@WebMvcTest
@ContextConfiguration(classes = BaseRestControllerTest.RestControllerTestConfiguration.class)
@ComponentScan("com.arassec.igor.web")
public abstract class BaseRestControllerTest {

    /**
     * Context configuration for REST-Controller tests. Provides message sources to test I18N during controller tests.
     */
    @Configuration
    @Import(WebConfiguration.class)
    public static class RestControllerTestConfiguration {

        /**
         * Provides the message sources for testing.
         *
         * @return A list of {@link MessageSource}s configured for testing.
         */
        @Bean
        public List<MessageSource> messageSources() {
            return List.of(IgorConfigHelper.createMessageSource("i18n/mapper"));
        }

    }

    /**
     * MVC-Mock for testing.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * The {@link ObjectMapper} to convert JSON.
     */
    @Autowired
    @Qualifier("objectMapper")
    protected ObjectMapper objectMapper;

    /**
     * Mock of the {@link ObjectMapper} to convert JSON for simulated job executions.
     */
    @MockBean
    @Qualifier("simulationObjectMapper")
    protected ObjectMapper simulationObjectMapper;

    /**
     * Mock of the igor component registry.
     */
    @MockBean
    protected IgorComponentRegistry igorComponentRegistry;

    /**
     * Mock of the service repository.
     */
    @MockBean
    protected ServiceRepository serviceRepository;

    /**
     * Mocked manager for Jobs.
     */
    @MockBean
    protected JobManager jobManager;

    /**
     * Manager for Services.
     */
    @MockBean
    protected ServiceManager serviceManager;

    /**
     * Converts the web response into the desired object.
     *
     * @param mvcResult The {@link MvcResult} to use as JSON source.
     * @param clazz     The target clazz to deserialize the response into.
     * @param <T>       The type of the target class.
     *
     * @return The converted object.
     */
    protected <T> T convert(MvcResult mvcResult, Class<T> clazz) {
        try {
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), clazz);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Exception during response conversion!", e);
        }
    }

    /**
     * Converts the web response into the desired object.
     *
     * @param mvcResult     The {@link MvcResult} to use as JSON source.
     * @param typeReference The target clazz to deserialize the response into.
     * @param <T>           The type of the target class.
     *
     * @return The converted object.
     */
    protected <T> T convert(MvcResult mvcResult, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeReference);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Exception during response conversion!", e);
        }
    }

}
