package com.sercan.device_service.device.domain.service;

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
    public Device update(UUID id, String name, String brand, DeviceState state) {
        validateId(id);
        validateName(name);
        validateBrand(brand);
        validateState(state);

        Device existing = getExistingDevice(id);

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
    public Device patch(UUID id, String name, String brand, DeviceState state) {
        validateId(id);

        Device existing = getExistingDevice(id);
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
    public void delete(UUID id) {
        validateId(id);

        Device existing = getExistingDevice(id);

        if (existing.state() == DeviceState.IN_USE) {
            throw new InUseDeviceDeletionException(id);
        }

        devicePersistencePort.deleteById(id);
    }

    @Override
    public Device getById(UUID id) {
        validateId(id);
        return getExistingDevice(id);
    }

    @Override
    public List<Device> getAll() {
        return devicePersistencePort.findAll();
    }

    @Override
    public List<Device> getByBrand(String brand) {
        validateBrand(brand);
        return devicePersistencePort.findByBrand(brand.trim());
    }

    @Override
    public List<Device> getByState(DeviceState state) {
        validateState(state);
        return devicePersistencePort.findByState(state);
    }

    private void validateId(UUID id) {
        if (id == null) {
            throw new DeviceValidationException("Device id must not be null");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DeviceValidationException("Device name must not be blank");
        }
    }

    private void validateBrand(String brand) {
        if (brand == null || brand.isBlank()) {
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
