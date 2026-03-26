package com.sercan.device_service.device.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonMappingException;

import com.sercan.device_service.device.adapter.in.rest.dto.response.ErrorResponse;
import com.sercan.device_service.device.adapter.in.rest.exception.SearchFilterValidationException;
import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.DeviceValidationException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.Arrays;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DeviceValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDeviceValidationException(DeviceValidationException ex) {
        return new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(InUseDeviceDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInUseDeviceDeletionException(InUseDeviceDeletionException ex) {
        return new ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(InUseDeviceModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInUseDeviceModificationException(InUseDeviceModificationException ex) {
        return new ErrorResponse(Instant.now(), HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(SearchFilterValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSearchFilterValidationException(SearchFilterValidationException ex) {
        return new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter: " + ex.getName();

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Object[] enumValues = ex.getRequiredType().getEnumConstants();
            message = "Invalid value for parameter '" + ex.getName()
                    + "'. Allowed values: " + java.util.Arrays.toString(enumValues);
        }

        return new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Malformed request body";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                String fieldName = invalidFormatException.getPath().stream()
                        .findFirst()
                        .map(JacksonException.Reference::getPropertyName)
                        .orElse("unknown");

                Object[] allowedValues = invalidFormatException.getTargetType().getEnumConstants();

                message = "Invalid value for field '" + fieldName + "'. Allowed values: "
                        + java.util.Arrays.toString(allowedValues);
            }
        }

        return new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                message
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String message = "Method '" + ex.getMethod() + "' is not supported for this endpoint.";

        if (ex.getSupportedMethods() != null && ex.getSupportedMethods().length > 0) {
            message += " Supported methods: " + Arrays.toString(ex.getSupportedMethods());
        }

        return new ErrorResponse(
                Instant.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                message
        );
    }
}
