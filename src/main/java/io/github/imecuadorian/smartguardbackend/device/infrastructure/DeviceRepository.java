package io.github.imecuadorian.smartguardbackend.device.infrastructure;

import io.github.imecuadorian.smartguardbackend.device.domain.Device;
import io.github.imecuadorian.smartguardbackend.device.domain.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    boolean existsByCode(String code);

    Optional<Device> findByCode(String code);

    List<Device> findAllByOrderByCodeAsc();

    @Query("""
            select device
            from Device device
            where (:status is null or device.status = :status)
            order by device.code asc
            """)
    List<Device> findAllFiltered(@Param("status") DeviceStatus status);
}
