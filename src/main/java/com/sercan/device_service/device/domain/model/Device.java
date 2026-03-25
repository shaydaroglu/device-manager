package com.sercan.device_service.device.domain.model;

import org.apache.logging.log4j.util.Strings;

import java.time.Instant;
import java.util.UUID;

public record Device(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        Instant creationTime,
        Instant updateTime
) {
    public Device {
        if (Strings.isBlank(name)) {
            throw new IllegalArgumentException("Device name cannot be null or blank.");
        }
        if (Strings.isBlank(brand)) {
            throw new IllegalArgumentException("Device brand cannot be null or blank.");
        }
        if (state == null) {
            throw new IllegalArgumentException("Device state cannot be null.");
        }
    }

    public static Device newDevice(String name, String brand, DeviceState state) {
        return new Device(
                UUID.randomUUID(),
                name,
                brand,
                state == null ? DeviceState.INACTIVE : state,
                null,
                null
        );
    }
}
