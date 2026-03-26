package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;
import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequestDto(
        @NotBlank(message = "Device name must not be blank")
        String name,

        @NotBlank(message = "Device brand must not be blank")
        String brand,

        DeviceState state
) {
}
