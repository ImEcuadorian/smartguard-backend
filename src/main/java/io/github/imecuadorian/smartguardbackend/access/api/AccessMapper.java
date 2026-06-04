package io.github.imecuadorian.smartguardbackend.access.api;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import io.github.imecuadorian.smartguardbackend.access.domain.AccessReader;
import io.github.imecuadorian.smartguardbackend.access.domain.RfidCard;
import org.springframework.stereotype.Component;

@Component
public class AccessMapper {
    public AccessReaderResponse toResponse(AccessReader reader) {
        return new AccessReaderResponse(
                reader.getId(),
                reader.getDevice().getId(),
                reader.getCode(),
                reader.getType(),
                reader.getLocation(),
                reader.getStatus(),
                reader.getCreatedAt(),
                reader.getUpdatedAt()
        );
    }

    public RfidCardResponse toResponse(RfidCard card) {
        return new RfidCardResponse(
                card.getId(),
                card.getUid(),
                card.getOwnerName(),
                card.getStatus(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    public AccessEventResponse toResponse(AccessEvent event) {
        return new AccessEventResponse(
                event.getId(),
                event.getDevice().getId(),
                event.getReader().getId(),
                event.getCard() == null ? null : event.getCard().getId(),
                event.getCardUid(),
                event.getResult(),
                event.getReason(),
                event.getOccurredAt(),
                event.getCreatedAt()
        );
    }
}
