package com.sercan.device_service.device.domain.port.in;

import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;

import java.util.UUID;

public interface DeviceManagementUseCase {
    Device create(String name, String brand, DeviceState state);
    Device update(String id, String name, String brand, DeviceState state);
    Device patch(String id, String name, String brand, DeviceState state);
    void delete(UUID id);
}
