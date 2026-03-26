package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.common.tests.BaseIntegrationTest;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device Persistence Seed Integration Tests")
@Sql(scripts = "/sql/cleanup_devices.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/seed_devices.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class DevicePersistenceSeedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DevicePersistenceAdapter devicePersistenceAdapter;

    private static final UUID IPHONE_15_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MACBOOK_PRO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID GALAXY_S24_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PIXEL_8_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    @DisplayName("should load seeded devices from PostgreSQL")
    void shouldLoadSeededDevicesFromPostgreSQL() {
        List<Device> allDevices = devicePersistenceAdapter.findAll();

        assertThat(allDevices).hasSize(12);
        assertThat(allDevices).extracting(Device::id)
                .contains(IPHONE_15_ID, MACBOOK_PRO_ID, GALAXY_S24_ID, PIXEL_8_ID);
    }

    @Test
    @DisplayName("should have devices table created by Flyway and seeded data applied")
    void shouldHaveDevicesTableCreatedByFlyway() {
        Device existingDevice = devicePersistenceAdapter.findById(IPHONE_15_ID).orElseThrow();

        assertThat(existingDevice.id()).isEqualTo(IPHONE_15_ID);
        assertThat(existingDevice.name()).isEqualTo("iPhone 15");
        assertThat(existingDevice.brand()).isEqualTo("Apple");
        assertThat(existingDevice.state()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("should return all devices from seeded test data")
    void shouldReturnAllDevices() {
        List<Device> allDevices = devicePersistenceAdapter.findAll();

        assertThat(allDevices).hasSize(12);
    }

    @Test
    @DisplayName("should retrieve seeded device by id")
    void shouldRetrieveSeededDeviceById() {
        Device device = devicePersistenceAdapter.findById(MACBOOK_PRO_ID).orElseThrow();

        assertThat(device.name()).isEqualTo("MacBook Pro");
        assertThat(device.brand()).isEqualTo("Apple");
        assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    @DisplayName("should contain seeded in-use Samsung device")
    void shouldContainSeededInUseSamsungDevice() {
        Device device = devicePersistenceAdapter.findById(GALAXY_S24_ID).orElseThrow();

        assertThat(device.name()).isEqualTo("Galaxy S24");
        assertThat(device.brand()).isEqualTo("Samsung");
        assertThat(device.state()).isEqualTo(DeviceState.IN_USE);
    }
}
