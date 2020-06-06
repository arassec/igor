package com.arassec.igor.web.controller;

import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.validation.UniqueConnectorName;
import com.arassec.igor.core.util.validation.UniqueJobName;
import com.arassec.igor.web.mapper.WebMapperKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles exceptions in REST-Controllers and returns an appropriate HTTP status.
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * The object mapper to create component instances from JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * The {@link MessageSource} for I18N.
     */
    private final MessageSource messageSource;

    /**
     * {@link IgorException}s are converted into "INTERNAL_SERVER_ERROR", although it might not be an internal error, but e.g. a
     * failed connector call.
     *
     * @param runtimeException The exception causing this method to handle it.
     * @param webRequest       The web request.
     *
     * @return A {@link ResponseEntity} with an appropriate HTTP status code.
     */
    @ExceptionHandler(value = {IgorException.class, IllegalStateException.class})
    protected ResponseEntity<Object> handleInternalServerError(RuntimeException runtimeException, WebRequest webRequest) {
        Map<String, Object> result = new HashMap<>();
        result.put(WebMapperKey.GENERAL_ERROR.getKey(), runtimeException.getMessage());
        log.warn("Caught exception during request!", runtimeException);
        return handleExceptionInternal(runtimeException, result,
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
        Map<String, Object> result = new HashMap<>();
        result.put(WebMapperKey.GENERAL_ERROR.getKey(), runtimeException.getMessage());
        return handleExceptionInternal(runtimeException, result,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

    /**
     * Handles validation exceptions and formats the result as JSON.
     *
     * @param methodArgumentNotValidException The exception.
     * @param headers                         HTTP-Headers.
     * @param status                          HTTP-Status.
     * @param webRequest                      The original web request.
     *
     * @return A {@link ResponseEntity} with a JSON object containing validation errors.
     */
    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValidException,
                                                                  @NonNull HttpHeaders headers, @NonNull HttpStatus status,
                                                                  @NonNull WebRequest webRequest) {

        Map<String, Map<String, String>> result = new HashMap<>();

        BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();

        Object target = bindingResult.getTarget(); // Job or Connector

        @SuppressWarnings("unchecked")
        Map<String, Object> validatedJson = objectMapper.convertValue(target, Map.class);

        List<ObjectError> allErrors = bindingResult.getAllErrors();

        allErrors.forEach(objectError -> {

            if (objectError instanceof FieldError) {
                appendFieldError(validatedJson, (FieldError) objectError, result);
            } else {
                // Special case: the custom "UniqueXyzName"-validators pass their validation result as ObjectError (because
                // bean validation doesn't support field validation with references to other fields, e.g. a job's ID).
                if (UniqueJobName.MESSAGE_KEY.equals(objectError.getDefaultMessage())
                        || UniqueConnectorName.MESSAGE_KEY.equals(objectError.getDefaultMessage())) {
                    appendUniqueNameViolationError(validatedJson, objectError, result);
                } else {
                    appendObjectError(objectError, result);
                }
            }

        });

        return handleExceptionInternal(methodArgumentNotValidException, result,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }

    /**
     * Appends an error object to the result map based on a {@link FieldError}.
     *
     * @param validatedJson The JSON containing the field causing the error.
     * @param fieldError    The {@link FieldError} causing the validation error.
     * @param target        The target map to add the error message to.
     */
    private void appendFieldError(Map<String, Object> validatedJson, FieldError fieldError, Map<String, Map<String, String>> target) {
        String id = extractId(validatedJson, fieldError.getField());
        if (id == null) {
            return;
        }
        if (!target.containsKey(id)) {
            target.put(id, new HashMap<>());
        }
        target.get(id).put(extractProperty(fieldError.getField()),
                Optional.ofNullable(fieldError.getDefaultMessage()).orElse("invalid value"));
    }

    /**
     * Appends an error object to the result map based on a custom igor name validation failure.
     *
     * @param validatedJson The JSON containing the field causing the error.
     * @param objectError   The {@link ObjectError} causing the validation error.
     * @param target        The target map to add the error message to.
     */
    private void appendUniqueNameViolationError(Map<String, Object> validatedJson, ObjectError objectError,
                                                Map<String, Map<String, String>> target) {
        String id = (String) validatedJson.get("id");
        if (!target.containsKey(id)) {
            target.put(id, new HashMap<>());
        }
        String errorMessage = Optional.ofNullable(objectError.getDefaultMessage()).orElse("name already in use");
        target.get(id).put("name", messageSource.getMessage(errorMessage, null,
                LocaleContextHolder.getLocale()));
    }

    /**
     * Appends a general error message to the result map based on an {@link ObjectError}.
     *
     * @param objectError The {@link ObjectError} causing the validation error.
     * @param target      The target map to add the error message to.
     */
    private void appendObjectError(ObjectError objectError, Map<String, Map<String, String>> target) {
        if (!target.containsKey(WebMapperKey.GENERAL_VALIDATION_ERRORS.getKey())) {
            target.put(WebMapperKey.GENERAL_VALIDATION_ERRORS.getKey(), new HashMap<>());
        }
        target.get(WebMapperKey.GENERAL_VALIDATION_ERRORS.getKey()).put(objectError.getObjectName(),
                Optional.ofNullable(objectError.getDefaultMessage()).orElse("validation error"));
    }

    /**
     * Extracts the ID of the component causing the validation error.
     *
     * @param target         The target object, i.e. a Job or Connector in JSON form.
     * @param validationPath The JSON-Path to the property causing the validation error.
     *
     * @return The ID of the component containing the invalid property.
     */
    private String extractId(Map<String, Object> target, String validationPath) {
        String jsonPath = validationPath.substring(0, validationPath.lastIndexOf('.') + 1) + "id";
        try {
            return JsonPath.parse(target).read(jsonPath);
        } catch (PathNotFoundException e) {
            // Fallback, to return something:
            return validationPath.replace(".", "_");
        }
    }

    /**
     * Extracts a property name from the supplied validation path.
     *
     * @param validationPath The JSON-Path to the property.
     *
     * @return The property's name.
     */
    private String extractProperty(String validationPath) {
        if (validationPath.contains(".")) {
            String[] parts = validationPath.split("\\.");
            return parts[parts.length - 1];
        } else {
            return validationPath;
        }
    }

}
