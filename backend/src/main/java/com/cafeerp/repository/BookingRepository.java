package com.cafeerp.repository;

import com.cafeerp.entity.Booking;
import com.cafeerp.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);

    List<Booking> findByStatusOrderByStartTimeDesc(BookingStatus status);

    List<Booking> findAllByOrderByStartTimeDesc();

    boolean existsByPcStationId(Long pcStationId);

    @Query("""
            select b from Booking b
            where b.pcStation.id = :pcStationId
            and b.status in :activeStatuses
            and b.startTime < :endTime
            and b.endTime > :startTime
            """)
    List<Booking> findOverlappingBookings(
            @Param("pcStationId") Long pcStationId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );
}
