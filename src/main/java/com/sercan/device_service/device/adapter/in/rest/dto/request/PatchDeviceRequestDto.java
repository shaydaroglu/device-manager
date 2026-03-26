package com.sercan.device_service.device.adapter.in.rest.dto.request;

import com.sercan.device_service.device.domain.model.DeviceState;

public record PatchDeviceRequestDto(
        String name,
        String brand,
        DeviceState state
) {
}
