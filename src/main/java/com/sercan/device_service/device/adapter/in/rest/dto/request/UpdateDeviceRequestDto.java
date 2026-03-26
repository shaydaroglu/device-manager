package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Schema(
        name = "UpdateDeviceRequest",
        description = "Request payload for fully updating an existing device. All fields are required and will replace existing values. Use PATCH endpoint for partial updates"
)
public record UpdateDeviceRequestDto(
        @Schema(
                description = "Human-readable device name",
                example = "Galaxy S24"
        )
        @NotBlank(message = "Device name must not be blank")
        String name,

        @Schema(
                description = "Device brand or manufacturer",
                example = "Samsung"
        )
        @NotBlank(message = "Device brand must not be blank")
        String brand,

        @Schema(
                description = "New device state",
                example = "IN_USE",
                allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"}
        )
        @NotNull(message = "Device state must not be blank")
        DeviceState state
) {
}
