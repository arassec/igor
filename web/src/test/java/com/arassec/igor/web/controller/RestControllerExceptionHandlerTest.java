package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.IgorConfigHelper;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.validation.UniqueConnectorName;
import com.arassec.igor.core.util.validation.UniqueJobName;
import com.arassec.igor.web.mapper.WebMapperKey;
import com.arassec.igor.web.test.TestConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RestControllerExceptionHandler}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Controller exception handling tests.")
class RestControllerExceptionHandlerTest {

    /**
     * The class under test.
     */
    private RestControllerExceptionHandler exceptionHandler;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        exceptionHandler = new RestControllerExceptionHandler(new ObjectMapper(),
                IgorConfigHelper.createMessageSource("i18n/core-messages"));
    }

    /**
     * Tests handling igor exceptions.
     */
    @Test
    @DisplayName("Tests handling igor exceptions.")
    void testHandleIgorException() {
        ResponseEntity<Object> responseEntity = exceptionHandler.handleInternalServerError(new IgorException("igor-exception"),
                mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(json);
        Object key = json.get(WebMapperKey.GENERAL_ERROR.getKey());
        assertNotNull(key);
        assertEquals("igor-exception", key);
    }

    /**
     * Tests handling bad requests.
     */
    @Test
    @DisplayName("Tests handling bad requests.")
    void testHandleBadRequest() {
        ResponseEntity<Object> responseEntity = exceptionHandler.handleBadRequest(new IllegalArgumentException("illegal-argument-exception"), mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(json);
        Object key = json.get(WebMapperKey.GENERAL_ERROR.getKey());
        assertNotNull(key);
        assertEquals("illegal-argument-exception", key);
    }

    /**
     * Tests handling field validation errors.
     */
    @Test
    @DisplayName("Tests handling field validation errors.")
    void testHandleMethodArgumentNotValidFieldErrors() {
        Job job = Job.builder().id("job-id").name("job-name").build();

        BindingResult bindingResultMock = mock(BindingResult.class);
        when(bindingResultMock.getTarget()).thenReturn(job);
        when(bindingResultMock.getAllErrors()).thenReturn(List.of(
                new FieldError("job", "name", "name-validation-failed"),
                new FieldError("provider", "unknown.field", "id-validation-failed")
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResultMock);

        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exception,
                mock(HttpHeaders.class), HttpStatus.OK, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> resultJson = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(resultJson);
        assertEquals("{job-id={name=name-validation-failed}, unknown_field={field=id-validation-failed}}", resultJson.toString());
    }

    /**
     * Tests handling unique name validation errors.
     */
    @Test
    @DisplayName("Tests handling unique name validation errors.")
    void testHandleMethodArgumentNotValidUniqueNameErrors() {
        // Job name validation error handling:
        Job job = Job.builder().id("job-id").name("job-name").build();

        BindingResult bindingResultMock = mock(BindingResult.class);
        when(bindingResultMock.getTarget()).thenReturn(job);
        when(bindingResultMock.getAllErrors()).thenReturn(List.of(
                new ObjectError("job", UniqueJobName.MESSAGE_KEY)
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResultMock);

        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exception,
                mock(HttpHeaders.class), HttpStatus.OK, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{job-id={name=a job with the same name already exists}}",
                String.valueOf(responseEntity.getBody()));

        // Connector name validation error handling:
        TestConnector connector = new TestConnector();
        connector.setId("connector-id");
        when(bindingResultMock.getTarget()).thenReturn(connector);
        when(bindingResultMock.getAllErrors()).thenReturn(List.of(
                new ObjectError("connector", UniqueConnectorName.MESSAGE_KEY)
        ));

        responseEntity = exceptionHandler.handleMethodArgumentNotValid(exception,
                mock(HttpHeaders.class), HttpStatus.OK, mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{connector-id={name=a connector with the same name already exists}}",
                String.valueOf(responseEntity.getBody()));
    }

    /**
     * Tests handling object validation errors.
     */
    @Test
    @DisplayName("Tests handling object validation errors.")
    void testHandleMethodArgumentNotValidObjectErrors() {

        BindingResult bindingResultMock = mock(BindingResult.class);
        when(bindingResultMock.getTarget()).thenReturn(Job.builder().build());
        when(bindingResultMock.getAllErrors()).thenReturn(List.of(
                new ObjectError("alpha", "alpha-invalid"),
                new ObjectError("beta", "beta-invalid")
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResultMock);

        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exception,
                mock(HttpHeaders.class), HttpStatus.OK, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> resultJson = (Map<String, Object>) responseEntity.getBody();
        assertNotNull(resultJson);
        assertEquals("{generalValidationErrors={alpha=alpha-invalid, beta=beta-invalid}}", resultJson.toString());
    }

}