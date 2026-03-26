package com.sercan.device_service.device.domain.service;

import com.sercan.device_service.device.adapter.in.rest.dto.request.DeviceFilter;
import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.DeviceValidationException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import com.sercan.device_service.device.domain.port.in.DeviceManagementUseCase;
import com.sercan.device_service.device.domain.port.in.DeviceQueryUseCase;
import com.sercan.device_service.device.domain.port.out.DevicePersistencePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@AllArgsConstructor
public class DeviceService implements DeviceManagementUseCase, DeviceQueryUseCase {
    private final DevicePersistencePort devicePersistencePort;


    @Override
    public Device create(String name, String brand, DeviceState state) {
        validateName(name);
        validateBrand(brand);

        Device device = Device.newDevice(
                name.trim(),
                brand.trim(),
                state == null ? DeviceState.INACTIVE : state
        );

        return devicePersistencePort.save(device);
    }

    @Override
    public Device update(String id, String name, String brand, DeviceState state) {
        validateId(id);
        validateName(name);
        validateBrand(brand);
        validateState(state);

        UUID uuid = UUID.fromString(id);
        Device existing = getExistingDevice(uuid);

        validateDeviceCanBeChanged(existing, name, brand);

        Device updatedDevice = new Device(
                existing.id(),
                name.trim(),
                brand.trim(),
                state,
                existing.creationTime(),
                null
        );

        return devicePersistencePort.save(updatedDevice);
    }

    @Override
    public Device patch(String id, String name, String brand, DeviceState state) {
        validateId(id);
        UUID uuid = UUID.fromString(id);

        Device existing = getExistingDevice(uuid);
        String resolvedName = name != null ? name.trim() : existing.name();
        String resolvedBrand = brand != null ? brand.trim() : existing.brand();
        DeviceState resolvedState = state != null ? state : existing.state();

        validateName(name);
        validateBrand(brand);
        validateDeviceCanBeChanged(existing, resolvedName, resolvedBrand);

        Device patchedDevice = new Device(
                existing.id(),
                resolvedName,
                resolvedBrand,
                resolvedState,
                existing.creationTime(),
                existing.updateTime()
        );

        return devicePersistencePort.save(patchedDevice);
    }

    @Override
    public void delete(UUID uuid) {
        if(uuid == null) {
            throw new DeviceValidationException("Device id must not be null");
        }

        Device existing = getExistingDevice(uuid);

        if (existing.state() == DeviceState.IN_USE) {
            throw new InUseDeviceDeletionException(uuid);
        }

        devicePersistencePort.deleteById(uuid);
    }

    @Override
    public Device getById(String id) {
        validateId(id);
        return getExistingDevice(UUID.fromString(id));
    }

    @Override
    public List<Device> getAll() {
        return devicePersistencePort.findAll();
    }

    @Override
    public List<Device> findByFilter(DeviceFilter filter) {
        return devicePersistencePort.findByFilter(filter);
    }

    private void validateId(String id) {
        if (id == null) {
            throw new DeviceValidationException("Device id must not be null");
        }

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new DeviceValidationException("Invalid UUID format for device id: " + id);
        }
    }

    private void validateName(String name) {
        if (isBlank(name)) {
            throw new DeviceValidationException("Device name must not be blank");
        }
    }

    private void validateBrand(String brand) {
        if (isBlank(brand)) {
            throw new DeviceValidationException("Device brand must not be blank");
        }
    }

    private void validateState(DeviceState state) {
        if (state == null) {
            throw new DeviceValidationException("Device state must not be null");
        }
    }

    private Device getExistingDevice(UUID id) {
        return devicePersistencePort.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void validateDeviceCanBeChanged(Device existing, String newName, String newBrand) {
        if (existing.state() == DeviceState.IN_USE) {
            boolean nameChanged = !existing.name().equals(newName);
            boolean brandChanged = !existing.brand().equals(newBrand);

            if (nameChanged || brandChanged) {
                throw new InUseDeviceModificationException(existing.id());
            }
        }
    }

}
