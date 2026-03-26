package com.sercan.device_service.device.adapter.in.rest;

import com.sercan.device_service.device.adapter.in.rest.dto.request.CreateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.DeviceFilter;
import com.sercan.device_service.device.adapter.in.rest.dto.request.PatchDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.request.UpdateDeviceRequestDto;
import com.sercan.device_service.device.adapter.in.rest.dto.response.DeviceResponseDto;
import com.sercan.device_service.device.adapter.in.rest.dto.response.ErrorResponseDto;
import com.sercan.device_service.device.adapter.in.rest.validator.RequestValidator;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import com.sercan.device_service.device.domain.port.in.DeviceManagementUseCase;
import com.sercan.device_service.device.domain.port.in.DeviceQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Devices", description = "Operations for managing device resources")
@RestController
@RequestMapping("/api/v1/devices")
@AllArgsConstructor
public class DeviceController {

    private DeviceManagementUseCase deviceManagementUseCase;
    private DeviceQueryUseCase deviceQueryUseCase;
    private DeviceRestMapper deviceMapper;
    private RequestValidator requestValidator;

    @Operation(
            summary = "Create a new device",
            description = "Creates and persists a new device resource"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Device created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "Bad request",
                                    value = """
                                    {
                                      "timestamp": "2026-03-26T00:54:16.693163200Z",
                                      "status": 400,
                                      "message": "Invalid value for field 'state'. Allowed values: [AVAILABLE, IN_USE, INACTIVE]"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<DeviceResponseDto> createDevice(@Valid @RequestBody CreateDeviceRequestDto request) {
        log.info("Received create device request");
        Device createdDevice = deviceManagementUseCase.create(request.name(), request.brand(), request.state());

        var response = deviceMapper.deviceToDto(createdDevice);

        return ResponseEntity
                .created(URI.create("/api/v1/devices/" + createdDevice.id()))
                .body(response);
    }

    @Operation(
            summary = "Get a device by id",
            description = "Returns a single device by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Device retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "timestamp": "2026-03-26T01:00:00Z",
                                          "status": 400,
                                          "message": "Invalid UUID format for parameter: id"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "timestamp": "2026-03-26T01:00:00Z",
                                          "status": 404,
                                          "message": "Device not found with id: 550e8400-e29b-41d4-a716-446655440000"
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDto> getDeviceById(
            @Parameter(
                    description = "Unique device identifier in UUID format",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String id
    ) {
        log.debug("Received get device request for id='{}'", id);
        Device device = deviceQueryUseCase.getById(id);

        return ResponseEntity.ok(deviceMapper.deviceToDto(device));
    }

    @Operation(
            summary = "Get all devices",
            description = "Returns all persisted devices"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devices retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceResponseDto.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<DeviceResponseDto>> getAllDevices() {
        log.debug("Received get all devices request");
        var response = deviceQueryUseCase.getAll()
                .stream()
                .map(deviceMapper::deviceToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Search devices",
            description = """
                Searches devices using one or more optional filters.
                
                At least one filter must be provided.
                Filters can be combined.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Matching devices retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Search request is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "No filters",
                                            value = """
                                                {
                                                  "timestamp": "2026-03-26T01:00:00Z",
                                                  "status": 400,
                                                  "message": "At least one search filter must be provided"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid state",
                                            value = """
                                                {
                                                  "timestamp": "2026-03-26T01:00:00Z",
                                                  "status": 400,
                                                  "message": "Invalid value for parameter 'state'. Allowed values: [AVAILABLE, IN_USE, INACTIVE]"
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<DeviceResponseDto>> getDevices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) DeviceState state
    ) {
        requestValidator.validateSearchFilter(name, brand, state);

        DeviceFilter filter = new DeviceFilter(name, brand, state);
        log.debug("Received device search request filter={}", filter);

        List<Device> devices = deviceQueryUseCase.findByFilter(filter);

        List<DeviceResponseDto> response = devices.stream()
                .map(deviceMapper::deviceToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Fully update a device",
            description = """
                Fully updates an existing device.
                
                Business rules:
                - Creation time cannot be updated.
                - Name and brand cannot be changed if the device is currently IN_USE.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Device updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload or invalid UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Device is in use and name or brand cannot be modified",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "timestamp": "2026-03-26T01:00:00Z",
                                          "status": 409,
                                          "message": "Name and brand cannot be updated when device is in use"
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/{id}")
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

    @Operation(
            summary = "Partially update a device",
            description = """
                Partially updates an existing device.
                Only provided fields will be changed.
                
                Business rules:
                - Creation time cannot be updated.
                - Name and brand cannot be changed if the device is currently IN_USE.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Device updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload, invalid UUID format, or empty patch request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Device is in use and name or brand cannot be modified",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponseDto> patchDevice(
            @PathVariable String id,
            @RequestBody PatchDeviceRequestDto request) {
        requestValidator.validatePatch(request);

        Device patchedDevice = deviceManagementUseCase.patch(
                id,
                request.name(),
                request.brand(),
                request.state());

        return ResponseEntity.ok(deviceMapper.deviceToDto(patchedDevice));
    }

    @Operation(
            summary = "Delete a device",
            description = """
                Deletes a device by id.
                
                Business rules:
                - Devices in IN_USE state cannot be deleted.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Device deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Device is in use and cannot be deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "timestamp": "2026-03-26T01:00:00Z",
                                          "status": 409,
                                          "message": "In-use devices cannot be deleted",
                                          "path": "/api/v1/devices/550e8400-e29b-41d4-a716-446655440000"
                                        }
                                        """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable @NotNull UUID id) {
        deviceManagementUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
