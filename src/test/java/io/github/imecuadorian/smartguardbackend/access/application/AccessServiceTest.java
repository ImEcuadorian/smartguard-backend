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

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
