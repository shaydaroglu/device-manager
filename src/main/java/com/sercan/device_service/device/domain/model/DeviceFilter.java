package com.sercan.device_service.device.domain.model;

public record DeviceFilter(
        String name,
        String brand,
        DeviceState state
) {
}
