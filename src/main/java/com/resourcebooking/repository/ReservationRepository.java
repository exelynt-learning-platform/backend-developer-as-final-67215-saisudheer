package com.resourcebooking.repository;

import com.resourcebooking.entity.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
                JpaSpecificationExecutor<Reservation> {

    /**
     * Checks whether a resource has an overlapping active reservation.
     *
     * CANCELLED reservations are ignored.
     */
    @Query("""
            SELECT CASE
                WHEN COUNT(r) > 0 THEN true
                ELSE false
            END
            FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Checks for overlapping reservations while excluding
     * the reservation currently being updated.
     *
     * This prevents a reservation from conflicting with itself.
     */
    @Query("""
            SELECT CASE
                WHEN COUNT(r) > 0 THEN true
                ELSE false
            END
            FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.id <> :reservationId
              AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservationExcludingId(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("reservationId") Long reservationId
    );
}