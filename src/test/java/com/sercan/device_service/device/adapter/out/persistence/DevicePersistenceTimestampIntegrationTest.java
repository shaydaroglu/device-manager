package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.common.tests.BaseIntegrationTest;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Device Persistence Timestamp Integration Tests")
public class DevicePersistenceTimestampIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DevicePersistenceAdapter devicePersistenceAdapter;

    private static final String TEST_NAME = "Test Device";
    private static final String TEST_BRAND = "Test Brand";

    @Test
    @DisplayName("should set creation_time when device is created")
    void shouldSetCreationTime() {
        Instant beforeSave = Instant.now().minusSeconds(1);
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

        Device savedDevice = devicePersistenceAdapter.save(device);
        Instant afterSave = Instant.now().plusSeconds(1);

        assertThat(savedDevice.creationTime()).isNotNull();
        assertThat(savedDevice.creationTime()).isAfterOrEqualTo(beforeSave);
        assertThat(savedDevice.creationTime()).isBeforeOrEqualTo(afterSave);
    }

    @Test
    @DisplayName("should set update_time close to creation_time on creation")
    void shouldSetUpdateTimeEqualToCreationTimeOnCreation() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

        Device savedDevice = devicePersistenceAdapter.save(device);

        assertThat(savedDevice.updateTime()).isNotNull();
        assertThat(savedDevice.updateTime()).isCloseTo(savedDevice.creationTime(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("should update update_time when device is modified")
    void shouldUpdateUpdateTimeOnModification() throws InterruptedException {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);
        Device savedDevice = devicePersistenceAdapter.save(device);
        Instant originalUpdateTime = savedDevice.updateTime();

        Thread.sleep(1200);

        Device updatedDevice = new Device(
                savedDevice.id(),
                "Updated Name",
                TEST_BRAND,
                DeviceState.IN_USE,
                savedDevice.creationTime(),
                savedDevice.updateTime()
        );

        Device persistedUpdate = devicePersistenceAdapter.save(updatedDevice);

        assertThat(persistedUpdate.updateTime()).isAfter(originalUpdateTime);
    }

    @Test
    @DisplayName("should not modify creation_time on update")
    void shouldNotModifyCreationTimeOnUpdate() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);
        Device savedDevice = devicePersistenceAdapter.save(device);
        Instant originalCreationTime = savedDevice.creationTime();

        Device updatedDevice = new Device(
                savedDevice.id(),
                TEST_NAME,
                TEST_BRAND,
                DeviceState.IN_USE,
                originalCreationTime,
                savedDevice.updateTime()
        );

        Device persistedUpdate = devicePersistenceAdapter.save(updatedDevice);

        assertThat(persistedUpdate.creationTime()).isEqualTo(originalCreationTime);
    }

    @Test
    @DisplayName("should persist timestamps accurately in PostgreSQL")
    void shouldPersistTimestampsAccurately() {
        Device device = Device.newDevice(TEST_NAME, TEST_BRAND, DeviceState.AVAILABLE);

        Device savedDevice = devicePersistenceAdapter.save(device);
        Device retrievedDevice = devicePersistenceAdapter.findById(savedDevice.id()).orElseThrow();

        assertThat(retrievedDevice.creationTime()).isEqualTo(savedDevice.creationTime());
        assertThat(retrievedDevice.updateTime()).isEqualTo(savedDevice.updateTime());
    }
}
