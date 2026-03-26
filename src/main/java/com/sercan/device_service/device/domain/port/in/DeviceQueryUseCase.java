package com.sercan.device_service.device.domain.port.in;

import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.domain.model.Device;

import java.util.List;

public interface DeviceQueryUseCase {
    Device getById(String id);
    List<Device> getAll();
    List<Device> findByFilter(DeviceFilter filter);
}
