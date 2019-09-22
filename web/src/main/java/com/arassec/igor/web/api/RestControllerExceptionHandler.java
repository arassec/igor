package com.arassec.igor.web.api;

import com.arassec.igor.core.model.service.ServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handles exceptions in REST-Controllers and returns an appropriate HTTP status.
 */
@ControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Error indicating that a name for a service or job already exists.
     */
    public static final String NAME_ALREADY_EXISTS_ERROR = "NAME_ALREADY_EXISTS_ERROR";

    /**
     * {@link ServiceException}s are converted into "INTERNAL_SERVER_ERROR", although it might not be an internal
     * error, but e.g. a failed service call.
     *
     * @param runtimeException The exception causing this method to handle it.
     * @param webRequest       The web request.
     * @return A {@link ResponseEntity} with an appropriate HTTP status code.
     */
    @ExceptionHandler(value = {ServiceException.class})
    protected ResponseEntity<Object> handleServiceException(
            RuntimeException runtimeException, WebRequest webRequest) {
        String bodyOfResponse = runtimeException.getMessage();
        return handleExceptionInternal(runtimeException, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    /**
     * {@link IllegalArgumentException}s are converted into "BAD_REQUEST".
     *
     * @param runtimeException The exception causing this method to handle it.
     * @param webRequest       The web request.
     * @return A {@link ResponseEntity} with an appropriate HTTP status code.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleBadRequest(
            RuntimeException runtimeException, WebRequest webRequest) {
        String bodyOfResponse = runtimeException.getMessage();
        return handleExceptionInternal(runtimeException, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

}
