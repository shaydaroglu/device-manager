package com.sercan.device_service.device.domain.port.in;

import com.sercan.device_service.device.adapter.in.rest.dto.request.DeviceFilter;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;

import java.util.List;
import java.util.UUID;

public interface DeviceQueryUseCase {
    Device getById(String id);
    List<Device> getAll();
    List<Device> findByFilter(DeviceFilter filter);
}
