package io.github.imecuadorian.smartguardbackend.access.infrastructure;

import io.github.imecuadorian.smartguardbackend.access.domain.AccessReader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessReaderRepository extends JpaRepository<AccessReader, UUID> {
    boolean existsByCode(String code);

    Optional<AccessReader> findByCode(String code);

    List<AccessReader> findAllByOrderByCodeAsc();
}
