package com.sercan.device_service.device.domain.exception;

import java.util.UUID;

public class InUseDeviceModificationException extends RuntimeException {
    public InUseDeviceModificationException(UUID deviceId) {
        super("Device with id: " + deviceId + " is currently in use and cannot be modified.");
    }
}
