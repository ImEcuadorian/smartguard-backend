package io.github.imecuadorian.smartguardbackend.access.infrastructure;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {
    List<AccessEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);

    List<AccessEvent> findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(Instant from, Pageable pageable);

    List<AccessEvent> findByOccurredAtLessThanEqualOrderByOccurredAtDesc(Instant to, Pageable pageable);

    List<AccessEvent> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant from, Instant to, Pageable pageable);
}
