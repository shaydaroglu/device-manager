package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PatchDeviceRequest",
        description = "Request payload for partially updating a device. Only provided fields will be updated"
)
public record PatchDeviceRequestDto(
        @Schema(
                description = "Updated device name",
                example = "MacBook Pro 16"
        )
        String name,

        @Schema(
                description = "Updated device brand",
                example = "Apple"
        )
        String brand,

        @Schema(
                description = "Updated device state",
                example = "INACTIVE",
                allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"}
        )
        DeviceState state
) {
}
