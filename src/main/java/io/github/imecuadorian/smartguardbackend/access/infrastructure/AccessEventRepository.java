package io.github.imecuadorian.smartguardbackend.access.infrastructure;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {
    List<AccessEvent> findAllByOrderByOccurredAtDesc();

    @Query("""
            select event
            from AccessEvent event
            where (:from is null or event.occurredAt >= :from)
              and (:to is null or event.occurredAt <= :to)
            order by event.occurredAt desc
            """)
    List<AccessEvent> findEvents(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
