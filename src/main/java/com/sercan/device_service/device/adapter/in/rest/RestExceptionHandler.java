package com.sercan.device_service.device.adapter.in.rest;

import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.DeviceValidationException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

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
}
