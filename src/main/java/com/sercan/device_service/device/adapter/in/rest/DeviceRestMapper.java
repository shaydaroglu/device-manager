package com.sercan.device_service.device.adapter.in.rest;

import com.sercan.device_service.device.adapter.in.rest.dto.response.DeviceResponseDto;
import com.sercan.device_service.device.domain.model.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceRestMapper {

    public DeviceResponseDto deviceToDto(final Device device) {
        return new DeviceResponseDto(
                device.id(),
                device.name(),
                device.brand(),
                device.state(),
                device.creationTime(),
                device.updateTime()
        );
    }
}
