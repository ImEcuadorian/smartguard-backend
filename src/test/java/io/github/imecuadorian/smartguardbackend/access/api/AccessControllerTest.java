package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.application.AccessService;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderStatus;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReaderType;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessResult;
import io.github.imecuadorian.smartguardbackend.access.domain.RfidCardStatus;
import io.github.imecuadorian.smartguardbackend.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AccessControllerTest {

    private static final UUID DEVICE_ID = UUID.fromString("f6f928d2-1d8c-4ab1-9b8d-ec1a3d2f4f64");
    private static final UUID READER_ID = UUID.fromString("ba9bf8c5-f507-4630-b7ad-9ee1d1f2f690");
    private static final UUID CARD_ID = UUID.fromString("8ea286aa-5e2e-42b2-8ec6-51bb5ac4b6fb");
    private static final UUID EVENT_ID = UUID.fromString("e9550996-e24b-4c06-91b0-d1dabfdd6096");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccessService accessService;

    @Test
    void createReaderReturnsCreatedReader() throws Exception {
        when(accessService.createReader(any(CreateAccessReaderRequest.class))).thenReturn(new AccessReaderResponse(
                READER_ID,
                DEVICE_ID,
                "rfid-main",
                AccessReaderType.RFID_RC522,
                "Main entrance",
                AccessReaderStatus.ACTIVE,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/access/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", DEVICE_ID,
                                "code", "rfid-main",
                                "type", "RFID_RC522",
                                "location", "Main entrance"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("rfid-main"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCardReturnsCreatedCard() throws Exception {
        when(accessService.createCard(any(CreateRfidCardRequest.class))).thenReturn(new RfidCardResponse(
                CARD_ID,
                "A1:B2:C3:D4",
                "Mauricio",
                RfidCardStatus.ACTIVE,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/access/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "uid", "A1:B2:C3:D4",
                                "ownerName", "Mauricio"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid").value("A1:B2:C3:D4"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void scanCardReturnsAccessEvent() throws Exception {
        when(accessService.scan(any(AccessScanRequest.class))).thenReturn(new AccessEventResponse(
                EVENT_ID,
                DEVICE_ID,
                READER_ID,
                CARD_ID,
                "A1:B2:C3:D4",
                AccessResult.GRANTED,
                "Card authorized",
                Instant.parse("2026-06-03T20:00:00Z"),
                null
        ));

        mockMvc.perform(post("/api/v1/access/events/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "readerCode", "rfid-main",
                                "cardUid", "A1:B2:C3:D4"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("GRANTED"))
                .andExpect(jsonPath("$.reason").value("Card authorized"));
    }
}
