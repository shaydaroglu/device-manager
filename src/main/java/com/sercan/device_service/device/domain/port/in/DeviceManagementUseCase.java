package com.sercan.device_service.device.domain.port.in;

import com.sercan.device_service.device.domain.model.Device;

public interface DeviceManagementUseCase {
    Device create(String name, String brand, String state);
    Device update(String id, String name, String brand, String state);
    Device patch(String id, String name, String brand, String state);
    void delete(String id);
}
