package com.sercan.device_service.device.adapter.in.rest;

import com.sercan.device_service.device.adapter.in.rest.dto.response.ErrorResponseDto;
import com.sercan.device_service.device.adapter.in.rest.exception.SearchFilterValidationException;
import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.DeviceValidationException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import io.swagger.v3.oas.annotations.Hidden;
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

@Hidden
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DeviceValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleDeviceValidationException(DeviceValidationException ex) {
        return new ErrorResponseDto(Instant.now(), HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return new ErrorResponseDto(Instant.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(InUseDeviceDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleInUseDeviceDeletionException(InUseDeviceDeletionException ex) {
        return new ErrorResponseDto(Instant.now(), HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(InUseDeviceModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleInUseDeviceModificationException(InUseDeviceModificationException ex) {
        return new ErrorResponseDto(Instant.now(), HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(SearchFilterValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleSearchFilterValidationException(SearchFilterValidationException ex) {
        return new ErrorResponseDto(Instant.now(), HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for parameter: " + ex.getName();

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Object[] enumValues = ex.getRequiredType().getEnumConstants();
            message = "Invalid value for parameter '" + ex.getName()
                    + "'. Allowed values: " + java.util.Arrays.toString(enumValues);
        }

        return new ErrorResponseDto(Instant.now(), HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleHttpMessageNotReadable(
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

        return new ErrorResponseDto(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                message
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponseDto handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String message = "Method '" + ex.getMethod() + "' is not supported for this endpoint.";

        if (ex.getSupportedMethods() != null && ex.getSupportedMethods().length > 0) {
            message += " Supported methods: " + Arrays.toString(ex.getSupportedMethods());
        }

        return new ErrorResponseDto(
                Instant.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                message
        );
    }
}
