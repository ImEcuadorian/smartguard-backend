package io.github.imecuadorian.smartguardbackend.actuator.application;

import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorCommandResponse;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorMapper;
import io.github.imecuadorian.smartguardbackend.actuator.api.ActuatorResponse;
import io.github.imecuadorian.smartguardbackend.actuator.api.CreateActuatorCommandRequest;
import io.github.imecuadorian.smartguardbackend.actuator.api.CreateActuatorRequest;
import io.github.imecuadorian.smartguardbackend.actuator.domain.Actuator;
import io.github.imecuadorian.smartguardbackend.actuator.domain.ActuatorCommand;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorCommandRepository;
import io.github.imecuadorian.smartguardbackend.actuator.infrastructure.ActuatorRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActuatorService {

    private final DeviceRepository deviceRepository;
    private final ActuatorRepository actuatorRepository;
    private final ActuatorCommandRepository commandRepository;
    private final ActuatorMapper actuatorMapper;
    private final RealtimeNotifier realtimeNotifier;
    private final ActuatorCommandPublisher commandPublisher;

    public ActuatorService(DeviceRepository deviceRepository, ActuatorRepository actuatorRepository,
                           ActuatorCommandRepository commandRepository, ActuatorMapper actuatorMapper) {
        this(deviceRepository, actuatorRepository, commandRepository, actuatorMapper, RealtimeNotifier.noop(),
                ActuatorCommandPublisher.noop());
    }

    @Autowired
    public ActuatorService(DeviceRepository deviceRepository, ActuatorRepository actuatorRepository,
                           ActuatorCommandRepository commandRepository, ActuatorMapper actuatorMapper,
                           RealtimeNotifier realtimeNotifier, ActuatorCommandPublisher commandPublisher) {
        this.deviceRepository = deviceRepository;
        this.actuatorRepository = actuatorRepository;
        this.commandRepository = commandRepository;
        this.actuatorMapper = actuatorMapper;
        this.realtimeNotifier = realtimeNotifier;
        this.commandPublisher = commandPublisher;
    }

    public ActuatorResponse create(CreateActuatorRequest request) {
        String code = request.code().trim();
        if (actuatorRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Actuator code already exists");
        }

        Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        var actuator = new Actuator(device, code, request.name(), request.type(), request.location());
        return actuatorMapper.toResponse(actuatorRepository.save(actuator));
    }

    @Transactional(readOnly = true)
    public List<ActuatorResponse> findAll() {
        return actuatorRepository.findAllByOrderByCodeAsc().stream().map(actuatorMapper::toResponse).toList();
    }

    public ActuatorCommandResponse createCommand(UUID actuatorId, CreateActuatorCommandRequest request) {
        var actuator = actuatorRepository.findById(actuatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actuator not found"));
        var command = new ActuatorCommand(actuator, request.command(), request.payload());
        var savedCommand = commandRepository.save(command);
        commandPublisher.publish(savedCommand);
        var response = actuatorMapper.toResponse(savedCommand);
        realtimeNotifier.actuatorCommandCreated(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ActuatorCommandResponse> findCommands(UUID actuatorId) {
        if (!actuatorRepository.existsById(actuatorId)) {
            throw new ResourceNotFoundException("Actuator not found");
        }

        return commandRepository.findAllByActuatorIdOrderByCreatedAtDesc(actuatorId)
                .stream()
                .map(actuatorMapper::toResponse)
                .toList();
    }
}
