package io.github.imecuadorian.smartguardbackend.device.application;

import io.github.imecuadorian.smartguardbackend.device.api.CreateDeviceRequest;
import io.github.imecuadorian.smartguardbackend.device.api.DeviceMapper;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(
                deviceRepository,
                new DeviceMapper(),
                passwordEncoder,
                () -> "device-api-key"
        );
    }

    @Test
    void createDeviceStoresAnActiveDeviceWithGeneratedApiKeyHash() {
        var request = new CreateDeviceRequest(
                "esp32-001",
                "ESP32 Main Door",
                "Main entrance",
                "192.168.1.50",
                "1.0.0"
        );

        when(deviceRepository.existsByCode("esp32-001")).thenReturn(false);
        when(passwordEncoder.encode("device-api-key")).thenReturn("hashed-device-api-key");
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = deviceService.create(request);

        assertThat(response.device().code()).isEqualTo("esp32-001");
        assertThat(response.device().status()).isEqualTo(DeviceStatus.ACTIVE);
        assertThat(response.apiKey()).isEqualTo("device-api-key");
        verify(deviceRepository).save(argThat(device -> "hashed-device-api-key".equals(device.getApiKeyHash())));
    }

    @Test
    void createDeviceRejectsDuplicateCode() {
        var request = new CreateDeviceRequest(
                "esp32-001",
                "ESP32 Main Door",
                "Main entrance",
                null,
                null
        );

        when(deviceRepository.existsByCode("esp32-001")).thenReturn(true);

        assertThatThrownBy(() -> deviceService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Device code already exists");
    }

    @Test
    void findByIdThrowsWhenDeviceDoesNotExist() {
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.findById(DEVICE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Device not found");
    }

    @Test
    void authenticateDeviceMarksDeviceAsSeenWhenApiKeyMatches() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        device.updateApiKeyHash("hashed-device-api-key");

        when(deviceRepository.findByCode("esp32-001")).thenReturn(Optional.of(device));
        when(passwordEncoder.matches("device-api-key", "hashed-device-api-key")).thenReturn(true);

        var response = deviceService.authenticateDevice("esp32-001", "device-api-key");

        assertThat(response.code()).isEqualTo("esp32-001");
        assertThat(response.lastSeenAt()).isNotNull();
    }

    @Test
    void authenticateDeviceRejectsInvalidApiKey() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        device.updateApiKeyHash("hashed-device-api-key");

        when(deviceRepository.findByCode("esp32-001")).thenReturn(Optional.of(device));
        when(passwordEncoder.matches("bad-key", "hashed-device-api-key")).thenReturn(false);

        assertThatThrownBy(() -> deviceService.authenticateDevice("esp32-001", "bad-key"))
                .isInstanceOf(DeviceAuthenticationException.class)
                .hasMessage("Invalid device credentials");
    }
}
