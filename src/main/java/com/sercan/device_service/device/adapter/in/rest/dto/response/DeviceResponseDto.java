package com.sercan.device_service.device.adapter.in.rest.dto.response;

import com.sercan.device_service.device.domain.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        name = "DeviceResponse",
        description = "Represents a device returned by the API"
)
public record DeviceResponseDto(
        @Schema(
                description = "Unique device identifier",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Human-readable device name",
                example = "iPhone 15 Pro"
        )
        String name,

        @Schema(
                description = "Device brand or manufacturer",
                example = "Apple"
        )
        String brand,

        @Schema(
                description = "Current device state",
                example = "AVAILABLE",
                allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"}
        )
        DeviceState state,

        @Schema(
                description = "Timestamp when the device was created",
                example = "2026-03-26T00:01:44.095663Z"
        )
        Instant createdAt,

        @Schema(
                description = "Timestamp when the device was last updated",
                example = "2026-03-26T00:01:44.095663Z"
        )
        Instant updatedAt
) {
}
