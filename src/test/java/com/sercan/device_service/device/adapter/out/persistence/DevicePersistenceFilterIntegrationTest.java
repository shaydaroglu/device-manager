package com.sercan.device_service.device.adapter.out.persistence;

import com.sercan.device_service.common.tests.BaseIntegrationTest;
import com.sercan.device_service.device.adapter.out.persistence.entity.DeviceJpaEntity;
import com.sercan.device_service.device.adapter.out.persistence.repository.DeviceJpaRepository;
import com.sercan.device_service.device.domain.model.DeviceFilter;
import com.sercan.device_service.device.domain.model.Device;
import com.sercan.device_service.device.domain.model.DeviceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device Persistence Filter Integration Tests")
public class DevicePersistenceFilterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DevicePersistenceAdapter devicePersistenceAdapter;

    @Autowired
    private DeviceJpaRepository deviceJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private static final UUID IPHONE_15_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID IPHONE_14_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MACBOOK_PRO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setUp() {
        // Clear test-created devices before each test
        jdbcTemplate.update("DELETE FROM devices WHERE id NOT IN (?, ?, ?)", IPHONE_15_ID, IPHONE_14_ID, MACBOOK_PRO_ID);
        // Clear Hibernate session to ensure fresh reads
        entityManager.clear();
    }



    @Test
    @DisplayName("should filter devices by name")
    void shouldFilterByName() {
        Device created = devicePersistenceAdapter.save(Device.newDevice("Filter Name Device", "Filter Brand", DeviceState.AVAILABLE));

        DeviceFilter filter = new DeviceFilter("Filter Name Device", null, null);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).extracting(Device::id).contains(created.id());
        assertThat(result).anyMatch(device -> device.name().equals("Filter Name Device"));
    }

    @Test
    @DisplayName("should filter devices by name case-insensitively")
    void shouldFilterByNameCaseInsensitively() {
        Device created = devicePersistenceAdapter.save(Device.newDevice("Case Sensitive Name", "Filter Brand", DeviceState.AVAILABLE));

        DeviceFilter filter = new DeviceFilter("case sensitive name", null, null);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).extracting(Device::id).contains(created.id());
    }

    @Test
    @DisplayName("should filter devices by brand case-insensitively")
    void shouldFilterByBrandCaseInsensitive() {
        Device created1 = devicePersistenceAdapter.save(Device.newDevice("Brand Device 1", "Case Brand", DeviceState.AVAILABLE));
        Device created2 = devicePersistenceAdapter.save(Device.newDevice("Brand Device 2", "Case Brand", DeviceState.IN_USE));

        DeviceFilter filter = new DeviceFilter(null, "case brand", null);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).extracting(Device::id).contains(created1.id(), created2.id());
        assertThat(result).allMatch(device -> device.brand().equalsIgnoreCase("Case Brand"));
    }

    @Test
    @DisplayName("should filter devices by state")
    void shouldFilterByState() {
        devicePersistenceAdapter.save(Device.newDevice("State Device 1", "State Brand", DeviceState.IN_USE));
        devicePersistenceAdapter.save(Device.newDevice("State Device 2", "State Brand", DeviceState.IN_USE));

        DeviceFilter filter = new DeviceFilter(null, null, DeviceState.IN_USE);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(device -> device.state() == DeviceState.IN_USE);
    }

    @Test
    @DisplayName("should filter devices by all three criteria combined")
    void shouldFilterByAllCriteria() {
        Device created = devicePersistenceAdapter.save(Device.newDevice("Combined Device", "Combined Brand", DeviceState.IN_USE));
        devicePersistenceAdapter.save(Device.newDevice("Combined Device", "Other Brand", DeviceState.IN_USE));
        devicePersistenceAdapter.save(Device.newDevice("Other Device", "Combined Brand", DeviceState.IN_USE));

        DeviceFilter filter = new DeviceFilter("Combined Device", "combined brand", DeviceState.IN_USE);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).extracting(Device::id).contains(created.id());
        assertThat(result).anyMatch(device ->
                device.name().equals("Combined Device")
                        && device.brand().equals("Combined Brand")
                        && device.state() == DeviceState.IN_USE
        );
    }

    @Test
    @DisplayName("should trim filter values")
    void shouldTrimFilterValues() {
        Device created = devicePersistenceAdapter.save(Device.newDevice("Trim Device", "Trim Brand", DeviceState.AVAILABLE));

        DeviceFilter filter = new DeviceFilter("  Trim Device  ", "  Trim Brand  ", DeviceState.AVAILABLE);
        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).extracting(Device::id).contains(created.id());
    }

    @Test
    @DisplayName("should return all devices when filter is empty")
    void shouldReturnAllDevicesWhenFilterIsEmpty() {
        DeviceFilter filter = new DeviceFilter(null, null, null);

        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(Device::id).contains(IPHONE_15_ID, IPHONE_14_ID, MACBOOK_PRO_ID);
    }

    @Test
    @DisplayName("should return empty list when filter does not match any device")
    void shouldReturnEmptyListWhenFilterDoesNotMatch() {
        DeviceFilter filter = new DeviceFilter("Non Existing Device", "No Brand", DeviceState.INACTIVE);

        List<Device> result = devicePersistenceAdapter.findByFilter(filter);

        assertThat(result).isEmpty();
    }
}
