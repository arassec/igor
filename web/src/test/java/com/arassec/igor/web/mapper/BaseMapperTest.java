package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.repository.ServiceRepository;
import com.arassec.igor.core.util.IgorConfigHelper;
import com.arassec.igor.web.WebConfiguration;
import com.arassec.igor.web.test.TestAction;
import com.arassec.igor.web.test.TestProvider;
import com.arassec.igor.web.test.TestService;
import com.arassec.igor.web.test.TestTrigger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for mapping tests.
 */
public abstract class BaseMapperTest {

    /**
     * {@link ObjectMapper} for the web layer.
     */
    protected ObjectMapper objectMapper;

    /**
     * {@link ObjectMapper} for simulated job runs.
     */
    protected ObjectMapper simulationObjectMapper;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        ApplicationContext applicationContextMock = mock(ApplicationContext.class);
        when(applicationContextMock.getBean(eq(TestService.class))).thenReturn(new TestService());
        when(applicationContextMock.getBean(eq(TestTrigger.class))).thenReturn(new TestTrigger());
        when(applicationContextMock.getBean(eq(TestProvider.class))).thenReturn(new TestProvider());
        when(applicationContextMock.getBean(eq(TestAction.class))).thenReturn(new TestAction());

        IgorComponentRegistry igorComponentRegistry = new IgorComponentRegistry(List.of(new TestAction()), List.of(new TestProvider()),
                List.of(new TestTrigger()), List.of(new TestService()));
        igorComponentRegistry.setApplicationContext(applicationContextMock);
        igorComponentRegistry.afterPropertiesSet();

        ServiceRepository serviceRepositoryMock = mock(ServiceRepository.class);
        when(serviceRepositoryMock.findById(eq(TestService.SERVICE_ID))).thenReturn(new TestService());

        WebConfiguration webConfiguration = new WebConfiguration();

        objectMapper = webConfiguration.objectMapper(igorComponentRegistry, serviceRepositoryMock,
                webConfiguration.messageSource(List.of(IgorConfigHelper.createMessageSource("i18n/mapper"))));

        simulationObjectMapper = webConfiguration.simulationObjectMapper(igorComponentRegistry, serviceRepositoryMock,
                webConfiguration.messageSource(List.of(IgorConfigHelper.createMessageSource("i18n/mapper"))));
    }

    /**
     * Simple equality check for two JSON Strings.
     *
     * @param firstJson  The first json to test.
     * @param secondJson The second json to test.
     *
     * @return {@code true} if the corresponding JSON objects are equal, {@code false} otherwise.
     */
    protected boolean isJsonEqual(String firstJson, String secondJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode firstJsonNode = objectMapper.readTree(firstJson);
        JsonNode secondJsonNode = objectMapper.readTree(secondJson);
        return firstJsonNode.equals(secondJsonNode);
    }

}