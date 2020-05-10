package com.arassec.igor.web.controller;

import com.arassec.igor.core.util.IgorException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles exceptions in REST-Controllers and returns an appropriate HTTP status.
 */
@ControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Error indicating that a name for a connector or job already exists.
     */
    public static final String NAME_ALREADY_EXISTS_ERROR = "NAME_ALREADY_EXISTS_ERROR";

    /**
     * {@link IgorException}s are converted into "INTERNAL_SERVER_ERROR", although it might not be an internal error, but e.g. a
     * failed connector call.
     *
     * @param runtimeException The exception causing this method to handle it.
     * @param webRequest       The web request.
     *
     * @return A {@link ResponseEntity} with an appropriate HTTP status code.
     */
    @ExceptionHandler(value = {IgorException.class})
    protected ResponseEntity<Object> handleIgorException(RuntimeException runtimeException, WebRequest webRequest) {
        String bodyOfResponse = runtimeException.getMessage();
        return handleExceptionInternal(runtimeException, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    /**
     * {@link IllegalArgumentException}s are converted into "BAD_REQUEST".
     *
     * @param runtimeException The exception causing this method to handle it.
     * @param webRequest       The web request.
     *
     * @return A {@link ResponseEntity} with an appropriate HTTP status code.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleBadRequest(RuntimeException runtimeException, WebRequest webRequest) {
        String bodyOfResponse = runtimeException.getMessage();
        return handleExceptionInternal(runtimeException, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

    /**
     * Handles validation exceptions and formats the error message.
     *
     * @param methodArgumentNotValidException The exception.
     * @param headers                         HTTP-Headers.
     * @param status                          HTTP-Status.
     * @param webRequest                      The original web request.
     *
     * @return A {@link ResponseEntity} with an appropriate HTTP status code and a formatted error message.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValidException,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        List<String> validationErrors = new LinkedList<>();

        List<ObjectError> allErrors = methodArgumentNotValidException.getBindingResult().getAllErrors();
        allErrors.forEach(objectError -> {
            StringBuilder errorMessageBuilder = new StringBuilder();
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                errorMessageBuilder.append(fieldError.getObjectName());
                errorMessageBuilder.append(".");
                errorMessageBuilder.append(fieldError.getField());
                errorMessageBuilder.append(": ");
                errorMessageBuilder.append(fieldError.getDefaultMessage());
            } else {
                errorMessageBuilder.append(objectError.getObjectName());
                errorMessageBuilder.append(": ");
                errorMessageBuilder.append(objectError.getDefaultMessage());
            }
            String errorMessage = errorMessageBuilder.toString();
            if (errorMessage.length() > 200) {
                validationErrors.add(errorMessage.substring(0, 200) + "...");
            } else {
                validationErrors.add(errorMessage);
            }
        });

        return handleExceptionInternal(methodArgumentNotValidException, String.join(" / ", validationErrors),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

}
