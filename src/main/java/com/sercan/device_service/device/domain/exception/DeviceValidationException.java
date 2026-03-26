package com.sercan.device_service.device.domain.exception;

public class DeviceValidationException extends RuntimeException {
    public DeviceValidationException(String message) {
        super(message);
    }
}
