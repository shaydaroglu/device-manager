package com.sercan.device_service.device.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sercan.device_service.common.tests.TestLoggerExtension;
import com.sercan.device_service.device.adapter.in.rest.dto.request.CreateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.PatchDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.UpdateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.response.DeviceResponseDto;
import com.sercan.device_service.device.adapter.in.rest.validator.RequestValidator;
import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import com.sercan.device_service.device.domain.port.in.DeviceManagementUseCase;
import com.sercan.device_service.device.domain.port.in.DeviceQueryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(DeviceController.class)
@Import({RestExceptionHandler.class, com.fasterxml.jackson.databind.ObjectMapper.class, RequestValidator.class})
@ExtendWith(TestLoggerExtension.class)
@DisplayName("Device Controller Tests")
public class DeviceControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private DeviceManagementUseCase deviceManagementUseCase;
    @MockitoBean private DeviceQueryUseCase deviceQueryUseCase;
    @MockitoBean private DeviceRestMapper deviceRestMapper;

    @Test
    @DisplayName("should create device and return 201")
    void shouldCreateDevice() throws Exception {
        UUID id = UUID.randomUUID();
        Device device = new Device(id, "iPhone 15", "Apple", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "iPhone 15", "Apple", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z")
        );

        CreateDeviceRequestDto request = new CreateDeviceRequestDto("iPhone 15", "Apple", DeviceState.AVAILABLE);

        given(deviceManagementUseCase.create("iPhone 15", "Apple", DeviceState.AVAILABLE)).willReturn(device);
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/devices/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.state").value("AVAILABLE"));
    }

    @Test
    @DisplayName("should return 400 when create request has blank name")
    void shouldReturn400WhenCreateRequestHasBlankName() throws Exception {
        String body = """
                {
                  "name": "",
                  "brand": "Apple",
                  "state": "AVAILABLE"
                }
                """;

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Device name must not be blank"));
    }

    @Test
    @DisplayName("should return 400 when enum value is invalid in request body")
    void shouldReturn400WhenEnumValueIsInvalidInRequestBody() throws Exception {
        String body = """
                {
                  "name": "iPhone 15",
                  "brand": "Apple",
                  "state": "A"
                }
                """;

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for field 'state'. Allowed values: [AVAILABLE, IN_USE, INACTIVE]"));
    }

    @Test
    @DisplayName("should get device by id and return 200")
    void shouldGetDeviceById() throws Exception {
        UUID id = UUID.randomUUID();
        Device device = new Device(id, "MacBook Pro", "Apple", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "MacBook Pro", "Apple", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z")
        );

        given(deviceQueryUseCase.getById(id)).willReturn(device);
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("MacBook Pro"));
    }

    @Test
    @DisplayName("should return 404 when device is not found")
    void shouldReturn404WhenDeviceIsNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        given(deviceQueryUseCase.getById(id)).willThrow(new DeviceNotFoundException(id));

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Device not found with id: " + id));
    }

    @Test
    @DisplayName("should return 400 when UUID is invalid")
    void shouldReturn400WhenUuidIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/devices/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return all devices")
    void shouldReturnAllDevices() throws Exception {
        UUID id = UUID.randomUUID();

        Device device = new Device(id, "Pixel 8", "Google", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "Pixel 8", "Google", DeviceState.AVAILABLE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z")
        );

        given(deviceQueryUseCase.getAll()).willReturn(List.of(device));
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Pixel 8"));
    }

    @Test
    @DisplayName("should search devices and return 200")
    void shouldSearchDevices() throws Exception {
        UUID id = UUID.randomUUID();

        Device device = new Device(id, "Galaxy S24", "Samsung", DeviceState.IN_USE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "Galaxy S24", "Samsung", DeviceState.IN_USE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:00:00Z")
        );

        given(deviceQueryUseCase.findByFilter(any())).willReturn(List.of(device));
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/devices/search")
                        .param("brand", "Samsung")
                        .param("state", "IN_USE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Samsung"))
                .andExpect(jsonPath("$[0].state").value("IN_USE"));
    }

    @Test
    @DisplayName("should return 400 when search has no filters")
    void shouldReturn400WhenSearchHasNoFilters() throws Exception {
        mockMvc.perform(get("/api/v1/devices/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when search state query parameter is invalid")
    void shouldReturn400WhenSearchStateIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/devices/search")
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should update device and return 200")
    void shouldUpdateDevice() throws Exception {
        UUID id = UUID.randomUUID();

        Device device = new Device(id, "Updated Device", "Updated Brand", DeviceState.INACTIVE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:10:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "Updated Device", "Updated Brand", DeviceState.INACTIVE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:10:00Z")
        );

        UpdateDeviceRequestDto request = new UpdateDeviceRequestDto("Updated Device", "Updated Brand", DeviceState.INACTIVE);

        given(deviceManagementUseCase.update(id.toString(), "Updated Device", "Updated Brand", DeviceState.INACTIVE)).willReturn(device);
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(put("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Device"))
                .andExpect(jsonPath("$.brand").value("Updated Brand"))
                .andExpect(jsonPath("$.state").value("INACTIVE"));
    }

    @Test
    @DisplayName("should return 409 when updating in-use device name or brand")
    void shouldReturn409WhenUpdatingInUseDeviceNameOrBrand() throws Exception {
        UUID id = UUID.randomUUID();

        UpdateDeviceRequestDto request = new UpdateDeviceRequestDto("New Name", "New Brand", DeviceState.IN_USE);

        given(deviceManagementUseCase.update(id.toString(), "New Name", "New Brand", DeviceState.IN_USE))
                .willThrow(new InUseDeviceModificationException(id));

        mockMvc.perform(put("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should patch device and return 200")
    void shouldPatchDevice() throws Exception {
        UUID id = UUID.randomUUID();

        Device device = new Device(id, "Patched Device", "Apple", DeviceState.INACTIVE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:20:00Z"));

        DeviceResponseDto responseDto = new DeviceResponseDto(
                id, "Patched Device", "Apple", DeviceState.INACTIVE,
                Instant.parse("2026-03-26T00:00:00Z"),
                Instant.parse("2026-03-26T00:20:00Z")
        );

        PatchDeviceRequestDto request = new PatchDeviceRequestDto("Patched Device", null, DeviceState.INACTIVE);

        given(deviceManagementUseCase.patch(id.toString(), "Patched Device", null, DeviceState.INACTIVE)).willReturn(device);
        given(deviceRestMapper.deviceToDto(device)).willReturn(responseDto);

        mockMvc.perform(patch("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Device"))
                .andExpect(jsonPath("$.state").value("INACTIVE"));
    }

    @Test
    @DisplayName("should return 400 when patch body is empty")
    void shouldReturn400WhenPatchBodyIsEmpty() throws Exception {
        String body = "{}";

        mockMvc.perform(patch("/api/v1/devices/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should delete device and return 204")
    void shouldDeleteDevice() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return 409 when deleting in-use device")
    void shouldReturn409WhenDeletingInUseDevice() throws Exception {
        UUID id = UUID.randomUUID();

        org.mockito.BDDMockito.willThrow(new InUseDeviceDeletionException(id))
                .given(deviceManagementUseCase).delete(id);

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isConflict());
    }

}
