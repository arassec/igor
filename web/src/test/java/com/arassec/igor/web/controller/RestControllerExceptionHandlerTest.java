package com.arassec.igor.web.controller;

import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RestControllerExceptionHandler}.
 */
@DisplayName("Controler exception handling tests.")
class RestControllerExceptionHandlerTest {

    /**
     * The class under test.
     */
    private RestControllerExceptionHandler exceptionHandler = new RestControllerExceptionHandler();

    /**
     * Tests handling igor exceptions.
     */
    @Test
    @DisplayName("Tests handling igor exceptions.")
    void testHandleIgorException() {
        ResponseEntity<Object> responseEntity = exceptionHandler.handleIgorException(new IgorException("igor-exception"), mock(WebRequest.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("igor-exception", responseEntity.getBody());
    }

    /**
     * Tests handling bad requests.
     */
    @Test
    @DisplayName("Tests handling bad requests.")
    void testHandleBadRequest() {
        ResponseEntity<Object> responseEntity = exceptionHandler.handleBadRequest(new IllegalArgumentException("illegal-argument-exception"), mock(WebRequest.class));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("illegal-argument-exception", responseEntity.getBody());
    }

    /**
     * Tests handling validation exceptions.
     */
    @Test
    @DisplayName("Tests handling validation exceptions.")
    void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exceptionMock = mock(MethodArgumentNotValidException.class);

        BindingResult bindingResultMock = mock(BindingResult.class);

        when(bindingResultMock.getAllErrors()).thenReturn(List.of(new ObjectError("object-name", "object-default-message"),
                new FieldError("field-name", "field", "field-default-message")));
        when(exceptionMock.getBindingResult()).thenReturn(bindingResultMock);

        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exceptionMock,
                mock(HttpHeaders.class), HttpStatus.I_AM_A_TEAPOT, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("object-name: object-default-message / field-name.field: field-default-message", responseEntity.getBody());
    }

    /**
     * Tests limiting the validation message.
     */
    @Test
    @DisplayName("Tests validation exceptions message limit.")
    void testHandleMethodArgumentNotValidMessageLimit() {
        MethodArgumentNotValidException exceptionMock = mock(MethodArgumentNotValidException.class);

        BindingResult bindingResultMock = mock(BindingResult.class);

        when(bindingResultMock.getAllErrors()).thenReturn(List.of(new ObjectError("a", "b".repeat(210))));
        when(exceptionMock.getBindingResult()).thenReturn(bindingResultMock);

        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exceptionMock,
                mock(HttpHeaders.class), HttpStatus.I_AM_A_TEAPOT, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("a: " + "b".repeat(197) + "...", responseEntity.getBody());
    }

}