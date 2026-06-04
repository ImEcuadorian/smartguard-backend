package io.github.imecuadorian.smartguardbackend.device.application;

import io.github.imecuadorian.smartguardbackend.device.api.CreateDeviceRequest;
import io.github.imecuadorian.smartguardbackend.device.api.DeviceMapper;
import io.github.imecuadorian.smartguardbackend.device.api.DeviceRegistrationResponse;
import io.github.imecuadorian.smartguardbackend.device.api.DeviceResponse;
import io.github.imecuadorian.smartguardbackend.device.api.UpdateDeviceRequest;
import io.github.imecuadorian.smartguardbackend.device.api.UpdateDeviceStatusRequest;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;
    private final PasswordEncoder passwordEncoder;
    private final DeviceApiKeyGenerator apiKeyGenerator;

    public DeviceService(DeviceRepository deviceRepository,
                         DeviceMapper deviceMapper,
                         PasswordEncoder passwordEncoder,
                         DeviceApiKeyGenerator apiKeyGenerator) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
        this.passwordEncoder = passwordEncoder;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    public DeviceRegistrationResponse create(CreateDeviceRequest request) {
        String code = request.code().trim();
        if (deviceRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Device code already exists");
        }

        var device = new Device(
                code,
                request.name(),
                request.location(),
                request.ipAddress(),
                request.firmwareVersion()
        );
        String apiKey = apiKeyGenerator.generate();
        device.updateApiKeyHash(passwordEncoder.encode(apiKey));

        var savedDevice = deviceRepository.save(device);
        return new DeviceRegistrationResponse(deviceMapper.toResponse(savedDevice), apiKey);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll() {
        return findAll(null);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll(DeviceStatus status) {
        return deviceRepository.findAllFiltered(status)
                .stream()
                .map(deviceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse findById(UUID id) {
        return deviceMapper.toResponse(findDevice(id));
    }

    public DeviceResponse update(UUID id, UpdateDeviceRequest request) {
        var device = findDevice(id);
        validateCodeChange(device, request.code());

        device.updateDetails(
                request.code(),
                request.name(),
                request.location(),
                request.ipAddress(),
                request.firmwareVersion()
        );

        return deviceMapper.toResponse(device);
    }

    public DeviceResponse updateStatus(UUID id, UpdateDeviceStatusRequest request) {
        var device = findDevice(id);
        device.updateStatus(request.status());
        return deviceMapper.toResponse(device);
    }

    public DeviceResponse authenticateDevice(String code, String apiKey) {
        if (code == null || code.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new DeviceAuthenticationException("Invalid device credentials");
        }

        var device = deviceRepository.findByCode(code.trim())
                .orElseThrow(() -> new DeviceAuthenticationException("Invalid device credentials"));

        if (device.getApiKeyHash() == null || !passwordEncoder.matches(apiKey.trim(), device.getApiKeyHash())) {
            throw new DeviceAuthenticationException("Invalid device credentials");
        }

        device.markSeen(Instant.now());
        return deviceMapper.toResponse(device);
    }

    private Device findDevice(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
    }

    private void validateCodeChange(Device device, String requestedCode) {
        if (requestedCode == null) {
            return;
        }

        String normalizedCode = requestedCode.trim();
        if (!device.getCode().equals(normalizedCode) && deviceRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Device code already exists");
        }
    }
}
