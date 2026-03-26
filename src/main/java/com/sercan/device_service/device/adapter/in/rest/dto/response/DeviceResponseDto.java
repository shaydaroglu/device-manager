package com.sercan.device_service.device.adapter.in.rest.dto.response;

import com.sercan.device_service.device.domain.model.DeviceState;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponseDto(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        Instant createdAt,
        Instant updatedAt
) {
}
