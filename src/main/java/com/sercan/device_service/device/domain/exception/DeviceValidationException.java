package com.sercan.device_service.device.domain.exception;

import java.util.UUID;

public class DeviceValidationException extends RuntimeException {
    public DeviceValidationException(String message) {
        super(message);
    }
}
