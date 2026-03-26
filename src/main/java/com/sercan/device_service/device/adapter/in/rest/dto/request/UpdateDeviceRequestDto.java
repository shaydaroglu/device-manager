package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDeviceRequestDto(
        @NotBlank(message = "Device name must not be blank")
        String name,

        @NotBlank(message = "Device brand must not be blank")
        String brand,

        @NotNull(message = "Device state must not be blank")
        DeviceState state
) {
}
