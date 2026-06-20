package io.github.imecuadorian.smartguardbackend.access.application;

import io.github.imecuadorian.smartguardbackend.access.api.AccessMapper;
import io.github.imecuadorian.smartguardbackend.access.api.AccessScanRequest;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReader;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderType;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessResult;
import io.github.imecuadorian.smartguardbackend.access.domain.RfidCard;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessEventRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.AccessReaderRepository;
import io.github.imecuadorian.smartguardbackend.access.infrastructure.RfidCardRepository;
import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessReaderRepository readerRepository;

    @Mock
    private RfidCardRepository cardRepository;

    @Mock
    private AccessEventRepository eventRepository;

    private AccessService accessService;

    @BeforeEach
    void setUp() {
        accessService = new AccessService(null, readerRepository, cardRepository, eventRepository, new AccessMapper());
    }

    @Test
    void scanGrantsAccessForActiveCard() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var reader = new AccessReader(device, "rfid-main", AccessReaderType.RFID_RC522, "Main entrance");
        var card = new RfidCard("A1:B2:C3:D4", "Mauricio");

        when(readerRepository.findByCode("rfid-main")).thenReturn(Optional.of(reader));
        when(cardRepository.findByUid("A1:B2:C3:D4")).thenReturn(Optional.of(card));
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accessService.scan(new AccessScanRequest(
                "rfid-main",
                "A1:B2:C3:D4",
                Instant.parse("2026-06-03T20:00:00Z")
        ));

        assertThat(response.result()).isEqualTo(AccessResult.GRANTED);
        assertThat(response.reason()).isEqualTo("Card authorized");
    }

    @Test
    void scanDeniesAccessForUnknownCard() {
        var device = new Device("esp32-001", "ESP32 Main Door", null, null, null);
        var reader = new AccessReader(device, "rfid-main", AccessReaderType.RFID_RC522, "Main entrance");

        when(readerRepository.findByCode("rfid-main")).thenReturn(Optional.of(reader));
        when(cardRepository.findByUid("ZZ:ZZ:ZZ:ZZ")).thenReturn(Optional.empty());
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accessService.scan(new AccessScanRequest(
                "rfid-main",
                "ZZ:ZZ:ZZ:ZZ",
                Instant.parse("2026-06-03T20:00:00Z")
        ));

        assertThat(response.result()).isEqualTo(AccessResult.DENIED);
        assertThat(response.reason()).isEqualTo("Card is not registered");
    }

    @Test
    void findEventsWithoutDatesUsesUnfilteredQuery() {
        when(eventRepository.findAllByOrderByOccurredAtDesc(any(Pageable.class))).thenReturn(List.of());

        assertThat(accessService.findEvents(null, null, 5)).isEmpty();

        verify(eventRepository).findAllByOrderByOccurredAtDesc(any(Pageable.class));
    }

    @Test
    void findEventsWithOnlyFromUsesLowerBoundQuery() {
        var from = Instant.parse("2026-06-01T00:00:00Z");
        when(eventRepository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(
                eq(from), any(Pageable.class))).thenReturn(List.of());

        assertThat(accessService.findEvents(from, null, 5)).isEmpty();

        verify(eventRepository).findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(
                eq(from), any(Pageable.class));
    }

    @Test
    void findEventsWithOnlyToUsesUpperBoundQuery() {
        var to = Instant.parse("2026-06-30T23:59:59Z");
        when(eventRepository.findByOccurredAtLessThanEqualOrderByOccurredAtDesc(
                eq(to), any(Pageable.class))).thenReturn(List.of());

        assertThat(accessService.findEvents(null, to, 5)).isEmpty();

        verify(eventRepository).findByOccurredAtLessThanEqualOrderByOccurredAtDesc(
                eq(to), any(Pageable.class));
    }

    @Test
    void findEventsWithBothDatesUsesRangeQuery() {
        var from = Instant.parse("2026-06-01T00:00:00Z");
        var to = Instant.parse("2026-06-30T23:59:59Z");
        when(eventRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(
                eq(from), eq(to), any(Pageable.class))).thenReturn(List.of());

        assertThat(accessService.findEvents(from, to, 5)).isEmpty();

        verify(eventRepository).findByOccurredAtBetweenOrderByOccurredAtDesc(
                eq(from), eq(to), any(Pageable.class));
    }
}
