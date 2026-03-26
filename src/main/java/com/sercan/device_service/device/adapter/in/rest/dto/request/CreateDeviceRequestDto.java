package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "CreateDeviceRequest",
        description = "Request payload for creating a new device. 'name' and 'brand' are required fields. 'state' is optional and defaults to INACTIVE if not provided"
)
public record CreateDeviceRequestDto(
        @Schema(
                description = "Human-readable device name",
                example = "iPhone 15 Pro"
        )
        @NotBlank(message = "Device name must not be blank")
        String name,

        @Schema(
                description = "Device brand or manufacturer",
                example = "Apple"
        )
        @NotBlank(message = "Device brand must not be blank")
        String brand,

        @Schema(
                description = "Initial device state. If omitted, INACTIVE will be used",
                example = "AVAILABLE",
                allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"}
        )
        DeviceState state
) {
}
