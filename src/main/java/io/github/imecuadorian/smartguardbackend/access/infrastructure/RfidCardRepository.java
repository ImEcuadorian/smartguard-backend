package io.github.imecuadorian.smartguardbackend.access.infrastructure;

import io.github.imecuadorian.smartguardbackend.access.domain.RfidCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RfidCardRepository extends JpaRepository<RfidCard, UUID> {
    boolean existsByUid(String uid);

    Optional<RfidCard> findByUid(String uid);

    List<RfidCard> findAllByOrderByOwnerNameAsc();
}
