package com.sercan.device_service.device.domain.service;

import com.sercan.device_service.common.tests.BaseUnitTest;
import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.domain.exception.DeviceNotFoundException;
import com.sercan.device_service.device.domain.exception.DeviceValidationException;
import com.sercan.device_service.device.domain.exception.InUseDeviceDeletionException;
import com.sercan.device_service.device.domain.exception.InUseDeviceModificationException;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import com.sercan.device_service.device.domain.port.out.DevicePersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DeviceService Unit Tests")
class DeviceServiceTest extends BaseUnitTest {

    @Mock
    private DevicePersistencePort devicePersistencePort;

    @InjectMocks
    private DeviceService deviceService;

    private static final String TEST_NAME = "Test Device";
    private static final String TEST_BRAND = "Test Brand";
    private static final UUID TEST_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("create() method tests")
    class CreateTests {

        @Test
        @DisplayName("should create device successfully with all valid parameters")
        void shouldCreateDeviceSuccessfully() {
            // Given
            Device createdDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.INACTIVE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            // When
            Device result = deviceService.create(TEST_NAME, TEST_BRAND, DeviceState.INACTIVE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(TEST_NAME);
            assertThat(result.brand()).isEqualTo(TEST_BRAND);
            assertThat(result.state()).isEqualTo(DeviceState.INACTIVE);
            verify(devicePersistencePort).save(any(Device.class));
        }

        @Test
        @DisplayName("should create device with null state defaulting to INACTIVE")
        void shouldCreateDeviceWithNullStateDefaultingToInactive() {
            // Given
            Device createdDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.INACTIVE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            // When
            Device result = deviceService.create(TEST_NAME, TEST_BRAND, null);

            // Then
            assertThat(result.state()).isEqualTo(DeviceState.INACTIVE);
        }

        @Test
        @DisplayName("should trim name and brand when creating device")
        void shouldTrimNameAndBrand() {
            // Given
            String nameWithSpaces = "  Device Name  ";
            String brandWithSpaces = "  Device Brand  ";
            Device createdDevice = new Device(TEST_UUID, "Device Name", "Device Brand", DeviceState.INACTIVE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            // When
            deviceService.create(nameWithSpaces, brandWithSpaces, DeviceState.INACTIVE);

            // Then
            ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
            verify(devicePersistencePort).save(deviceCaptor.capture());
            Device savedDevice = deviceCaptor.getValue();
            assertThat(savedDevice.name()).isEqualTo("Device Name");
            assertThat(savedDevice.brand()).isEqualTo("Device Brand");
        }

        @Test
        @DisplayName("should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> deviceService.create("  ", TEST_BRAND, DeviceState.INACTIVE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device name must not be blank");
        }

        @Test
        @DisplayName("should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> deviceService.create(null, TEST_BRAND, DeviceState.INACTIVE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device name must not be blank");
        }

        @Test
        @DisplayName("should throw exception when brand is blank")
        void shouldThrowExceptionWhenBrandIsBlank() {
            assertThatThrownBy(() -> deviceService.create(TEST_NAME, "  ", DeviceState.INACTIVE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device brand must not be blank");
        }

        @Test
        @DisplayName("should throw exception when brand is null")
        void shouldThrowExceptionWhenBrandIsNull() {
            assertThatThrownBy(() -> deviceService.create(TEST_NAME, null, DeviceState.INACTIVE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device brand must not be blank");
        }
    }

    @Nested
    @DisplayName("update() method tests")
    class UpdateTests {

        @Test
        @DisplayName("should update device successfully with all valid parameters")
        void shouldUpdateDeviceSuccessfully() {
            // Given
            String newName = "Updated Device";
            String newBrand = "Updated Brand";
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device updatedDevice = new Device(TEST_UUID, newName, newBrand, DeviceState.IN_USE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(updatedDevice);

            // When
            Device result = deviceService.update(TEST_UUID.toString(), newName, newBrand, DeviceState.IN_USE);

            // Then
            assertThat(result.name()).isEqualTo(newName);
            assertThat(result.brand()).isEqualTo(newBrand);
            assertThat(result.state()).isEqualTo(DeviceState.IN_USE);
            verify(devicePersistencePort).findById(TEST_UUID);
            verify(devicePersistencePort).save(any(Device.class));
        }

        @Test
        @DisplayName("should throw exception when id is invalid UUID format")
        void shouldThrowExceptionWhenIdIsInvalidUUID() {
            assertThatThrownBy(() -> deviceService.update("invalid-uuid", TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Invalid UUID format");
        }

        @Test
        @DisplayName("should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> deviceService.update(null, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device id must not be null");
        }

        @Test
        @DisplayName("should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), "  ", TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device name must not be blank");
        }

        @Test
        @DisplayName("should throw exception when brand is blank")
        void shouldThrowExceptionWhenBrandIsBlank() {
            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), TEST_NAME, "  ", DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device brand must not be blank");
        }

        @Test
        @DisplayName("should throw exception when state is null")
        void shouldThrowExceptionWhenStateIsNull() {
            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, null))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device state must not be null");
        }

        @Test
        @DisplayName("should throw exception when device does not exist")
        void shouldThrowExceptionWhenDeviceNotFound() {
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when updating IN_USE device name")
        void shouldThrowExceptionWhenUpdatingInUseDeviceName() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When & Then
            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), "Different Name", TEST_BRAND, DeviceState.IN_USE))
                    .isInstanceOf(InUseDeviceModificationException.class);
            verify(devicePersistencePort, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when updating IN_USE device brand")
        void shouldThrowExceptionWhenUpdatingInUseDeviceBrand() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When & Then
            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), TEST_NAME, "Different Brand", DeviceState.IN_USE))
                    .isInstanceOf(InUseDeviceModificationException.class);
            verify(devicePersistencePort, never()).save(any());
        }

        @Test
        @DisplayName("should allow updating IN_USE device state without changing name or brand")
        void shouldAllowUpdatingInUseDeviceStateOnly() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            Device updatedDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(updatedDevice);

            // When
            Device result = deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

            // Then
            assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
            verify(devicePersistencePort).save(any(Device.class));
        }
    }

    @Nested
    @DisplayName("patch() method tests")
    class PatchTests {

        @Test
        @DisplayName("should patch device with all fields provided")
        void shouldPatchDeviceWithAllFields() {
            // Given
            String newName = "Patched Device";
            String newBrand = "Patched Brand";
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device patchedDevice = new Device(TEST_UUID, newName, newBrand, DeviceState.IN_USE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(patchedDevice);

            // When
            Device result = deviceService.patch(TEST_UUID.toString(), newName, newBrand, DeviceState.IN_USE);

            // Then
            assertThat(result.name()).isEqualTo(newName);
            assertThat(result.brand()).isEqualTo(newBrand);
            assertThat(result.state()).isEqualTo(DeviceState.IN_USE);
        }

        @Test
        @DisplayName("should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> deviceService.patch(null, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device id must not be null");
        }

        @Test
        @DisplayName("should throw exception when id is invalid UUID format")
        void shouldThrowExceptionWhenIdIsInvalidUUID() {
            assertThatThrownBy(() -> deviceService.patch("invalid-uuid", TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Invalid UUID format");
        }

        @Test
        @DisplayName("should throw exception when device not found")
        void shouldThrowExceptionWhenDeviceNotFound() {
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.patch(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(DeviceNotFoundException.class);
        }

        @Test
        @DisplayName("should patch device with only name provided, keeping existing brand and state")
        void shouldPatchDeviceWithOnlyName() {
            // Given
            String newName = "Patched Name";
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device patchedDevice = new Device(TEST_UUID, newName, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(patchedDevice);

            // When
            Device result = deviceService.patch(TEST_UUID.toString(), newName, null, null);

            // Then
            assertThat(result.name()).isEqualTo(newName);
            assertThat(result.brand()).isEqualTo(TEST_BRAND);
            assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("should patch device with only brand provided, keeping existing name and state")
        void shouldPatchDeviceWithOnlyBrand() {
            // Given
            String newBrand = "Patched Brand";
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device patchedDevice = new Device(TEST_UUID, TEST_NAME, newBrand, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(patchedDevice);

            // When
            Device result = deviceService.patch(TEST_UUID.toString(), null, newBrand, null);

            // Then
            assertThat(result.name()).isEqualTo(TEST_NAME);
            assertThat(result.brand()).isEqualTo(newBrand);
            assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("should patch device with only state provided, keeping existing name and brand")
        void shouldPatchDeviceWithOnlyState() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device patchedDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(patchedDevice);

            // When
            Device result = deviceService.patch(TEST_UUID.toString(), null, null, DeviceState.IN_USE);

            // Then
            assertThat(result.name()).isEqualTo(TEST_NAME);
            assertThat(result.brand()).isEqualTo(TEST_BRAND);
            assertThat(result.state()).isEqualTo(DeviceState.IN_USE);
        }

        @Test
        @DisplayName("should throw exception when patching IN_USE device with name change")
        void shouldThrowExceptionWhenPatchingInUseDeviceWithNameChange() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When & Then
            assertThatThrownBy(() -> deviceService.patch(TEST_UUID.toString(), "Different Name", null, null))
                    .isInstanceOf(InUseDeviceModificationException.class);
            verify(devicePersistencePort, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when patching IN_USE device with brand change")
        void shouldThrowExceptionWhenPatchingInUseDeviceWithBrandChange() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When & Then
            assertThatThrownBy(() -> deviceService.patch(TEST_UUID.toString(), null, "Different Brand", null))
                    .isInstanceOf(InUseDeviceModificationException.class);
            verify(devicePersistencePort, never()).save(any());
        }

        @Test
        @DisplayName("should allow patching IN_USE device state change without name or brand change")
        void shouldAllowPatchingInUseDeviceStateOnly() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            Device patchedDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(patchedDevice);

            // When
            Device result = deviceService.patch(TEST_UUID.toString(), null, null, DeviceState.AVAILABLE);

            // Then
            assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
            verify(devicePersistencePort).save(any(Device.class));
        }

        @Test
        @DisplayName("should throw exception when patching with blank name")
        void shouldThrowExceptionWhenPatchingWithBlankName() {
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            assertThatThrownBy(() -> deviceService.patch(TEST_UUID.toString(), "  ", null, null))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device name must not be blank");
        }

        @Test
        @DisplayName("should throw exception when patching with blank brand")
        void shouldThrowExceptionWhenPatchingWithBlankBrand() {
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            assertThatThrownBy(() -> deviceService.patch(TEST_UUID.toString(), null, "  ", null))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device brand must not be blank");
        }
    }

    @Nested
    @DisplayName("delete() method tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete device successfully when not IN_USE")
        void shouldDeleteDeviceSuccessfully() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When
            deviceService.delete(TEST_UUID);

            // Then
            verify(devicePersistencePort).findById(TEST_UUID);
            verify(devicePersistencePort).deleteById(TEST_UUID);
        }

        @Test
        @DisplayName("should delete INACTIVE device successfully")
        void shouldDeleteInactiveDeviceSuccessfully() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.INACTIVE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When
            deviceService.delete(TEST_UUID);

            // Then
            verify(devicePersistencePort).deleteById(TEST_UUID);
        }

        @Test
        @DisplayName("should throw exception when deleting IN_USE device")
        void shouldThrowExceptionWhenDeletingInUseDevice() {
            // Given
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            // When & Then
            assertThatThrownBy(() -> deviceService.delete(TEST_UUID))
                    .isInstanceOf(InUseDeviceDeletionException.class);
            verify(devicePersistencePort, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw exception when uuid is null")
        void shouldThrowExceptionWhenUuidIsNull() {
            assertThatThrownBy(() -> deviceService.delete(null))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device id must not be null");
            verify(devicePersistencePort, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw exception when device does not exist")
        void shouldThrowExceptionWhenDeviceNotFound() {
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.delete(TEST_UUID))
                    .isInstanceOf(DeviceNotFoundException.class);
            verify(devicePersistencePort, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getById() method tests")
    class GetByIdTests {

        @Test
        @DisplayName("should retrieve device successfully by valid id")
        void shouldRetrieveDeviceById() {
            // Given
            Device device = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(device));

            // When
            Device result = deviceService.getById(TEST_UUID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEST_UUID);
            assertThat(result.name()).isEqualTo(TEST_NAME);
            verify(devicePersistencePort).findById(TEST_UUID);
        }

        @Test
        @DisplayName("should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> deviceService.getById(null))
                    .isInstanceOf(DeviceValidationException.class)
                    .hasMessageContaining("Device id must not be null");
        }

        @Test
        @DisplayName("should throw exception when device does not exist")
        void shouldThrowExceptionWhenDeviceNotFound() {
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deviceService.getById(TEST_UUID))
                    .isInstanceOf(DeviceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAll() method tests")
    class GetAllTests {

        @Test
        @DisplayName("should retrieve all devices")
        void shouldRetrieveAllDevices() {
            // Given
            Device device1 = new Device(UUID.randomUUID(), "Device 1", "Brand 1", DeviceState.AVAILABLE, Instant.now(), null);
            Device device2 = new Device(UUID.randomUUID(), "Device 2", "Brand 2", DeviceState.IN_USE, Instant.now(), null);
            Device device3 = new Device(UUID.randomUUID(), "Device 3", "Brand 3", DeviceState.INACTIVE, Instant.now(), null);
            List<Device> devices = List.of(device1, device2, device3);

            when(devicePersistencePort.findAll()).thenReturn(devices);

            // When
            List<Device> result = deviceService.getAll();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(device1, device2, device3);
            verify(devicePersistencePort).findAll();
        }

        @Test
        @DisplayName("should return empty list when no devices exist")
        void shouldReturnEmptyListWhenNoDevices() {
            // Given
            when(devicePersistencePort.findAll()).thenReturn(List.of());

            // When
            List<Device> result = deviceService.getAll();

            // Then
            assertThat(result).isEmpty();
            verify(devicePersistencePort).findAll();
        }
    }

    @Nested
    @DisplayName("findByFilter() method tests")
    class FindByFilterTests {

        @Test
        @DisplayName("should find devices matching filter")
        void shouldFindDevicesByFilter() {
            // Given
            DeviceFilter filter = new DeviceFilter("Device 1", "Brand 1", DeviceState.AVAILABLE);
            Device device1 = new Device(UUID.randomUUID(), "Device 1", "Brand 1", DeviceState.AVAILABLE, Instant.now(), null);
            Device device2 = new Device(UUID.randomUUID(), "Device 2", "Brand 1", DeviceState.IN_USE, Instant.now(), null);
            List<Device> devices = List.of(device1, device2);

            when(devicePersistencePort.findByFilter(filter)).thenReturn(devices);

            // When
            List<Device> result = deviceService.findByFilter(filter);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(device1, device2);
            verify(devicePersistencePort).findByFilter(filter);
        }

        @Test
        @DisplayName("should return empty list when no devices match filter")
        void shouldReturnEmptyListWhenNoMatch() {
            // Given
            DeviceFilter filter = new DeviceFilter(null, null, null);
            when(devicePersistencePort.findByFilter(filter)).thenReturn(List.of());

            // When
            List<Device> result = deviceService.findByFilter(filter);

            // Then
            assertThat(result).isEmpty();
            verify(devicePersistencePort).findByFilter(filter);
        }
    }

    @Nested
    @DisplayName("Device Model Validation Tests")
    class DeviceModelValidationTests {

        @Test
        @DisplayName("should throw IllegalArgumentException when creating device with blank name in record")
        void shouldThrowWhenDeviceNameIsBlankInRecord() {
            assertThatThrownBy(() -> new Device(TEST_UUID, "  ", TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Device name cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when creating device with blank brand in record")
        void shouldThrowWhenDeviceBrandIsBlankInRecord() {
            assertThatThrownBy(() -> new Device(TEST_UUID, TEST_NAME, "  ", DeviceState.AVAILABLE, Instant.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Device brand cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when creating device with null state in record")
        void shouldThrowWhenDeviceStateIsNullInRecord() {
            assertThatThrownBy(() -> new Device(TEST_UUID, TEST_NAME, TEST_BRAND, null, Instant.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Device state cannot be null");
        }

        @Test
        @DisplayName("Device.newDevice() should create device with proper defaults")
        void shouldCreateDeviceWithProperDefaults() {
            Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

            assertThat(device).isNotNull();
            assertThat(device.id()).isNotNull();
            assertThat(device.name()).isEqualTo(TEST_NAME);
            assertThat(device.brand()).isEqualTo(TEST_BRAND);
            assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
            assertThat(device.creationTime()).isNull();
            assertThat(device.updateTime()).isNull();
        }

        @Test
        @DisplayName("Device.newDevice() should default to INACTIVE when state is null")
        void shouldDefaultToInactiveStateWhenNull() {
            Device device = Device.newDevice(TEST_NAME, TEST_BRAND, null);

            assertThat(device.state()).isEqualTo(DeviceState.INACTIVE);
        }
    }

    @Nested
    @DisplayName("Create Trimming and Edge Cases Tests")
    class CreateTrimmingTests {

        @Test
        @DisplayName("should trim only whitespace without changing actual content")
        void shouldTrimOnlyWhitespaceNotContent() {
            Device createdDevice = new Device(TEST_UUID, "Valid Name", "Valid Brand", DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            deviceService.create("  Valid Name  ", "  Valid Brand  ", DeviceState.AVAILABLE);

            ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
            verify(devicePersistencePort).save(captor.capture());

            assertThat(captor.getValue().name()).isEqualTo("Valid Name");
            assertThat(captor.getValue().brand()).isEqualTo("Valid Brand");
        }

        @Test
        @DisplayName("should preserve internal spaces when trimming")
        void shouldPreserveInternalSpaces() {
            Device createdDevice = new Device(TEST_UUID, "Device With Spaces", "Brand Name Here", DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            deviceService.create("  Device With Spaces  ", "  Brand Name Here  ", DeviceState.AVAILABLE);

            ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
            verify(devicePersistencePort).save(captor.capture());

            assertThat(captor.getValue().name()).isEqualTo("Device With Spaces");
            assertThat(captor.getValue().brand()).isEqualTo("Brand Name Here");
        }

        @Test
        @DisplayName("should call save method exactly once")
        void shouldCallSaveMethodExactlyOnce() {
            Device createdDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.save(any(Device.class))).thenReturn(createdDevice);

            deviceService.create(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

            verify(devicePersistencePort).save(any(Device.class));
        }
    }

    @Nested
    @DisplayName("Port Interaction and Exact Call Verification Tests")
    class PortInteractionTests {

        @Test
        @DisplayName("update should call findById with correct UUID")
        void shouldCallFindByIdWithCorrectUUID() {
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device updatedDevice = new Device(TEST_UUID, "New Name", TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(updatedDevice);

            deviceService.update(TEST_UUID.toString(), "New Name", TEST_BRAND, DeviceState.AVAILABLE);

            ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(devicePersistencePort).findById(uuidCaptor.capture());

            assertThat(uuidCaptor.getValue()).isEqualTo(TEST_UUID);
        }

        @Test
        @DisplayName("delete should call findById before deleteById")
        void shouldCallFindByIdBeforeDelete() {
            Device existingDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(existingDevice));

            deviceService.delete(TEST_UUID);

            verify(devicePersistencePort).findById(TEST_UUID);
            verify(devicePersistencePort).deleteById(TEST_UUID);
        }

        @Test
        @DisplayName("getAll should return list exactly as provided by port")
        void shouldReturnListExactlyAsProvidedByPort() {
            Device device1 = new Device(UUID.randomUUID(), "Device 1", "Brand 1", DeviceState.AVAILABLE, Instant.now(), null);
            Device device2 = new Device(UUID.randomUUID(), "Device 2", "Brand 2", DeviceState.IN_USE, Instant.now(), null);
            List<Device> expectedList = List.of(device1, device2);

            when(devicePersistencePort.findAll()).thenReturn(expectedList);

            List<Device> result = deviceService.getAll();

            assertThat(result).isInstanceOf(List.class);
            assertThat(result).containsExactlyElementsOf(expectedList);
        }
    }

    @Nested
    @DisplayName("State Transitions and Business Logic Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("should allow transitioning from AVAILABLE to IN_USE")
        void shouldAllowAvailableToInUseTransition() {
            Device availableDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device inUseDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(availableDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(inUseDevice);

            Device result = deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.IN_USE);

            assertThat(result.state()).isEqualTo(DeviceState.IN_USE);
        }

        @Test
        @DisplayName("should allow transitioning from IN_USE to AVAILABLE")
        void shouldAllowInUseToAvailableTransition() {
            Device inUseDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            Device availableDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(inUseDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(availableDevice);

            Device result = deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

            assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        }

        @Test
        @DisplayName("should allow transitioning from any state to INACTIVE")
        void shouldAllowAnyStateToInactiveTransition() {
            Device availableDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE, Instant.now(), null);
            Device inactiveDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.INACTIVE, Instant.now(), Instant.now());

            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(availableDevice));
            when(devicePersistencePort.save(any(Device.class))).thenReturn(inactiveDevice);

            Device result = deviceService.update(TEST_UUID.toString(), TEST_NAME, TEST_BRAND, DeviceState.INACTIVE);

            assertThat(result.state()).isEqualTo(DeviceState.INACTIVE);
        }

        @Test
        @DisplayName("should not allow name change even when state change is valid")
        void shouldNotAllowNameChangeEvenWithValidStateChange() {
            Device inUseDevice = new Device(TEST_UUID, TEST_NAME, TEST_BRAND, DeviceState.IN_USE, Instant.now(), null);
            when(devicePersistencePort.findById(TEST_UUID)).thenReturn(Optional.of(inUseDevice));

            assertThatThrownBy(() -> deviceService.update(TEST_UUID.toString(), "New Name", TEST_BRAND, DeviceState.AVAILABLE))
                    .isInstanceOf(InUseDeviceModificationException.class);
        }
    }
}
