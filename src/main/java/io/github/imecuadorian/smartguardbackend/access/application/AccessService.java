package io.github.imecuadorian.smartguardbackend.access.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessEventResponse;
import io.github.imecuadorian.smartguardbackend.access.api.AccessMapper;
import io.github.imecuadorian.smartguardbackend.access.api.AccessReaderResponse;
import io.github.imecuadorian.smartguardbackend.access.api.AccessScanRequest;
import io.github.imecuadorian.smartguardbackend.access.api.CreateAccessReaderRequest;
import io.github.imecuadorian.smartguardbackend.access.api.CreateRfidCardRequest;
import io.github.imecuadorian.smartguardbackend.access.api.RfidCardResponse;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReader;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderStatus;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessResult;
import io.github.imecuadorian.smartguardbackend.access.domain.RfidCard;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessEventRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessReaderRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.RfidCardRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.infrastructure.DeviceRepository;
import io.github.imecuadorian.smartguardbackend.realtime.application.RealtimeNotifier;
import io.github.imecuadorian.smartguardbackend.shared.error.DuplicateResourceException;
import io.github.imecuadorian.smartguardbackend.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AccessService {

    private final DeviceRepository deviceRepository;
    private final AccessReaderRepository readerRepository;
    private final RfidCardRepository cardRepository;
    private final AccessEventRepository eventRepository;
    private final AccessMapper accessMapper;
    private final RealtimeNotifier realtimeNotifier;

    public AccessService(DeviceRepository deviceRepository, AccessReaderRepository readerRepository,
                         RfidCardRepository cardRepository, AccessEventRepository eventRepository,
                         AccessMapper accessMapper) {
        this(deviceRepository, readerRepository, cardRepository, eventRepository, accessMapper, RealtimeNotifier.noop());
    }

    @Autowired
    public AccessService(DeviceRepository deviceRepository, AccessReaderRepository readerRepository,
                         RfidCardRepository cardRepository, AccessEventRepository eventRepository,
                         AccessMapper accessMapper, RealtimeNotifier realtimeNotifier) {
        this.deviceRepository = deviceRepository;
        this.readerRepository = readerRepository;
        this.cardRepository = cardRepository;
        this.eventRepository = eventRepository;
        this.accessMapper = accessMapper;
        this.realtimeNotifier = realtimeNotifier;
    }

    public AccessReaderResponse createReader(CreateAccessReaderRequest request) {
        String code = request.code().trim();
        if (readerRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Access reader code already exists");
        }

        Device device = deviceRepository.findById(request.deviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        var reader = new AccessReader(device, code, request.type(), request.location());
        return accessMapper.toResponse(readerRepository.save(reader));
    }

    public RfidCardResponse createCard(CreateRfidCardRequest request) {
        String uid = request.uid().trim();
        if (cardRepository.existsByUid(uid)) {
            throw new DuplicateResourceException("RFID card UID already exists");
        }

        var card = new RfidCard(uid, request.ownerName());
        return accessMapper.toResponse(cardRepository.save(card));
    }

    public AccessEventResponse scan(AccessScanRequest request) {
        var reader = readerRepository.findByCode(request.readerCode().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Access reader not found"));

        RfidCard card = cardRepository.findByUid(request.cardUid().trim()).orElse(null);
        AccessResult result;
        String reason;

        if (reader.getStatus() != AccessReaderStatus.ACTIVE) {
            result = AccessResult.DENIED;
            reason = "Reader is not active";
        } else if (card == null) {
            result = AccessResult.DENIED;
            reason = "Card is not registered";
        } else if (!card.isActive()) {
            result = AccessResult.DENIED;
            reason = "Card is not active";
        } else {
            result = AccessResult.GRANTED;
            reason = "Card authorized";
        }

        var event = new AccessEvent(reader, card, request.cardUid(), result, reason, request.occurredAt());
        var response = accessMapper.toResponse(eventRepository.save(event));
        realtimeNotifier.accessEventCreated(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AccessReaderResponse> findReaders() {
        return readerRepository.findAllByOrderByCodeAsc().stream().map(accessMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RfidCardResponse> findCards() {
        return cardRepository.findAllByOrderByOwnerNameAsc().stream().map(accessMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccessEventResponse> findEvents() {
        return findEvents(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<AccessEventResponse> findEvents(Instant from, Instant to, Integer limit) {
        int boundedLimit = limit == null ? 100 : Math.min(Math.max(limit, 1), 1000);
        return eventRepository.findEvents(from, to, PageRequest.of(0, boundedLimit))
                .stream()
                .map(accessMapper::toResponse)
                .toList();
    }
}
