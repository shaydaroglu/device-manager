package com.sercan.device_service.device.adapter.in.rest;

import com.sercan.device_service.device.adapter.in.rest.dto.request.CreateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.DeviceFilter;
import com.sercan.device_service.device.adapter.in.rest.dto.request.PatchDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.UpdateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.response.DeviceResponseDto;
import com.sercan.device_service.device.adapter.in.rest.exception.SearchFilterValidationException;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import com.sercan.device_service.device.domain.port.in.DeviceManagementUseCase;
import com.sercan.device_service.device.domain.port.in.DeviceQueryUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;


@RestController
@RequestMapping("/api/v1/devices")
@AllArgsConstructor
public class DeviceController {

    private DeviceManagementUseCase deviceManagementUseCase;
    private DeviceQueryUseCase deviceQueryUseCase;
    private DeviceRestMapper deviceMapper;

    @PostMapping
    public ResponseEntity<DeviceResponseDto> createDevice(@Valid @RequestBody CreateDeviceRequestDto request) {
        Device createdDevice = deviceManagementUseCase.create(request.name(), request.brand(), request.state());

        var response = deviceMapper.deviceToDto(createdDevice);

        return ResponseEntity
                .created(URI.create("/api/v1/devices/" + createdDevice.id()))
                .body(response);
    }

    @GetMapping("/{id:[0-9a-fA-F\\\\-]{36}}")
    public ResponseEntity<DeviceResponseDto> getDeviceById(@PathVariable String id) {
        Device device = deviceQueryUseCase.getById(id);

        return ResponseEntity.ok(deviceMapper.deviceToDto(device));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DeviceResponseDto>> getDevices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state
    ) {
        if(isBlank(name) && isBlank(brand) && state == null) {
            throw new SearchFilterValidationException();
        }

        List<Device> devices = deviceQueryUseCase.findByFilter(new DeviceFilter(name, brand, state));

        List<DeviceResponseDto> response = devices.stream()
                .map(deviceMapper::deviceToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id:[0-9a-fA-F\\\\-]{36}}")
    public ResponseEntity<DeviceResponseDto> updateDevice(
            @PathVariable String id,
            @Valid @RequestBody UpdateDeviceRequestDto request) {
        Device updatedDevice = deviceManagementUseCase.update(
                id,
                request.name(),
                request.brand(),
                request.state());

        return ResponseEntity.ok(deviceMapper.deviceToDto(updatedDevice));
    }

    @PatchMapping("/{id:[0-9a-fA-F\\\\-]{36}}")
    public ResponseEntity<DeviceResponseDto> patchDevice(
            @PathVariable String id,
            @Valid @RequestBody PatchDeviceRequestDto request) {
        Device patchedDevice = deviceManagementUseCase.patch(
                id,
                request.name(),
                request.brand(),
                request.state());

        return ResponseEntity.ok(deviceMapper.deviceToDto(patchedDevice));
    }

    @DeleteMapping("/{id:[0-9a-fA-F\\\\-]{36}}")
    public ResponseEntity<Void> deleteDevice(@PathVariable @NotNull UUID id) {
        deviceManagementUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
