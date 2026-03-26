package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.common.tests.BaseIntegrationTest;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device Persistence CRUD Integration Tests")
public class DevicePersistenceCrudIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DevicePersistenceAdapter devicePersistenceAdapter;

    @Autowired
    private DevicePersistenceMapper devicePersistenceMapper;

    private static final String TEST_NAME = "Test Device";
    private static final String TEST_BRAND = "Test Brand";

    @Test
    @DisplayName("should persist device to PostgreSQL")
    void shouldPersistDeviceToPostgreSQL() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

        Device savedDevice = devicePersistenceAdapter.save(device);

        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.id()).isEqualTo(device.id());
        assertThat(savedDevice.name()).isEqualTo(TEST_NAME);
        assertThat(savedDevice.brand()).isEqualTo(TEST_BRAND);
        assertThat(savedDevice.state()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(savedDevice.creationTime()).isNotNull();
        assertThat(savedDevice.updateTime()).isNotNull();
    }

    @Test
    @DisplayName("should retrieve persisted device from PostgreSQL")
    void shouldRetrievePersistedDevice() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);
        Device savedDevice = devicePersistenceAdapter.save(device);

        var retrievedDevice = devicePersistenceAdapter.findById(savedDevice.id());

        assertThat(retrievedDevice).isPresent();
        assertThat(retrievedDevice.get().id()).isEqualTo(savedDevice.id());
        assertThat(retrievedDevice.get().name()).isEqualTo(TEST_NAME);
        assertThat(retrievedDevice.get().brand()).isEqualTo(TEST_BRAND);
        assertThat(retrievedDevice.get().state()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("should delete device from PostgreSQL")
    void shouldDeleteDeviceFromPostgreSQL() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);
        Device savedDevice = devicePersistenceAdapter.save(device);

        devicePersistenceAdapter.deleteById(savedDevice.id());

        assertThat(devicePersistenceAdapter.findById(savedDevice.id())).isEmpty();
    }

    @Test
    @DisplayName("should map Device domain to DeviceJpaEntity correctly")
    void shouldMapDomainToEntity() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

        var entity = devicePersistenceMapper.toEntity(device);

        assertThat(entity.getId()).isEqualTo(device.id());
        assertThat(entity.getName()).isEqualTo(device.name());
        assertThat(entity.getBrand()).isEqualTo(device.brand());
        assertThat(entity.getState()).isEqualTo(device.state());
    }

    @Test
    @DisplayName("should preserve all device fields through save and retrieve cycle")
    void shouldPreserveAllFields() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.IN_USE);

        Device savedDevice = devicePersistenceAdapter.save(device);
        Device retrievedDevice = devicePersistenceAdapter.findById(savedDevice.id()).orElseThrow();

        assertThat(retrievedDevice.id()).isEqualTo(savedDevice.id());
        assertThat(retrievedDevice.name()).isEqualTo(device.name());
        assertThat(retrievedDevice.brand()).isEqualTo(device.brand());
        assertThat(retrievedDevice.state()).isEqualTo(device.state());
    }
}
