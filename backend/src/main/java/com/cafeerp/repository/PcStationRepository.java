package com.cafeerp.repository;

import com.cafeerp.entity.PcStation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PcStationRepository extends JpaRepository<PcStation, Long> {

    Optional<PcStation> findByLabelIgnoreCase(String label);

    boolean existsByLabelIgnoreCase(String label);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PcStation p where p.id = :id")
    Optional<PcStation> findByIdForUpdate(@Param("id") Long id);
}
