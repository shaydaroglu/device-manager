package com.sercan.device_service.device.domain.exception;

import java.util.UUID;

public class InUseDeviceDeletionException extends RuntimeException {
    public InUseDeviceDeletionException(UUID deviceId) {
        super("Device with id: " + deviceId + " is currently in use and cannot be deleted.");
    }
}
