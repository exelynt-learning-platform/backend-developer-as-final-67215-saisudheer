package com.resourcebooking.controller;

import com.resourcebooking.dto.reservation.ReservationRequest;
import com.resourcebooking.dto.reservation.ReservationResponse;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {

        this.reservationService = reservationService;
    }

    /**
     * USER + ADMIN
     *
     * Create reservation.
     *
     * IMPORTANT:
     * user identity comes from JWT/SecurityContext,
     * NOT from ReservationRequest.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        ReservationResponse response =
                reservationService.createReservation(
                        request,
                        authenticatedEmail
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * USER + ADMIN
     *
     * Get reservations.
     *
     * ADMIN:
     *     Gets all reservations.
     *
     * USER:
     *     Gets only their own reservations.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            Authentication authentication,

            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String sort) {

        validatePagination(page, size);

        Pageable pageable =
                createPageable(page, size, sort);

        String authenticatedEmail =
                authentication.getName();

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        Page<ReservationResponse> reservations =
                reservationService.getReservations(
                        authenticatedEmail,
                        isAdmin,
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                );

        return ResponseEntity.ok(reservations);
    }

    /**
     * USER + ADMIN
     *
     * Get reservation by ID.
     *
     * USER can only access their own reservation.
     * ADMIN can access any reservation.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );

        ReservationResponse response =
                reservationService.getReservationById(
                        id,
                        authenticatedEmail,
                        isAdmin
                );

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN ONLY
     *
     * Update reservation.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        ReservationResponse response =
                reservationService.updateReservation(
                        id,
                        request,
                        authenticatedEmail,
                        true
                );

        return ResponseEntity.ok(response);
    }

    /**
     * ADMIN ONLY
     *
     * Delete reservation.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String authenticatedEmail =
                authentication.getName();

        reservationService.deleteReservation(
                id,
                authenticatedEmail,
                true
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    /**
     * ADMIN ONLY
     *
     * Change reservation status.
     *
     * Example:
     * PUT /reservations/1/status?status=CONFIRMED
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {

        ReservationResponse response =
                reservationService.updateReservationStatus(
                        id,
                        status
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Creates Pageable object.
     *
     * Supported:
     *
     * page=0
     * size=10
     *
     * sort=price,desc
     *
     * sort=startTime,asc
     */
    private Pageable createPageable(
            int page,
            int size,
            String sort) {

        if (sort == null || sort.isBlank()) {

            return PageRequest.of(
                    page,
                    size
            );
        }

        String[] sortParts =
                sort.split(",");

        String property =
                sortParts[0].trim();

        Sort.Direction direction =
                Sort.Direction.ASC;

        if (sortParts.length > 1) {

            String directionValue =
                    sortParts[1].trim();

            if (directionValue.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            } else if (
                    !directionValue.equalsIgnoreCase("asc")) {

                throw new IllegalArgumentException(
                        "Sort direction must be 'asc' or 'desc'"
                );
            }
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, property)
        );
    }

    private void validatePagination(
            int page,
            int size) {

        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {

            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }
    }
}