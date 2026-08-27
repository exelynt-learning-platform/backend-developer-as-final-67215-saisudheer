package com.resourcebooking.service;

import com.resourcebooking.dto.reservation.ReservationRequest;
import com.resourcebooking.dto.reservation.ReservationResponse;
import com.resourcebooking.entity.Reservation;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.exception.ReservationNotFoundException;
import com.resourcebooking.exception.ResourceNotFoundException;
import com.resourcebooking.exception.UserNotFoundException;
import com.resourcebooking.repository.ReservationRepository;
import com.resourcebooking.repository.ResourceRepository;
import com.resourcebooking.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a reservation.
     *
     * The user is obtained from the authenticated JWT identity.
     * The client does NOT provide userId.
     */
    public ReservationResponse createReservation(
            ReservationRequest request,
            String authenticatedEmail) {

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        validatePrice(request.getPrice());

        User user = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        )
                );

        if (!Boolean.TRUE.equals(resource.getAvailable())) {

            throw new IllegalStateException(
                    "Resource is currently unavailable"
            );
        }

        /*
         * Check whether another active reservation overlaps
         * with the requested time period.
         */
        boolean overlappingReservation =
                reservationRepository.existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

        if (overlappingReservation) {

            throw new IllegalStateException(
                    "Resource is already booked for the selected time"
            );
        }

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());

        /*
         * USER cannot choose the initial status.
         */
        reservation.setStatus(
                ReservationStatus.PENDING
        );

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    /**
     * Get reservations.
     *
     * ADMIN:
     *     Can see all reservations.
     *
     * USER:
     *     Can see only their own reservations.
     */
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(
            String authenticatedEmail,
            boolean isAdmin,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        validatePriceFilter(minPrice, maxPrice);

        Specification<Reservation> specification =
                buildReservationSpecification(
                        authenticatedEmail,
                        isAdmin,
                        status,
                        minPrice,
                        maxPrice
                );

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get a reservation by ID.
     *
     * ADMIN:
     *     Can access any reservation.
     *
     * USER:
     *     Can access only their own reservation.
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(
            Long reservationId,
            String authenticatedEmail,
            boolean isAdmin) {

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        validateOwnership(
                reservation,
                authenticatedEmail,
                isAdmin
        );

        return mapToResponse(reservation);
    }

    /**
     * Update a reservation.
     *
     * ADMIN has permission to update reservations.
     */
    public ReservationResponse updateReservation(
            Long reservationId,
            ReservationRequest request,
            String authenticatedEmail,
            boolean isAdmin) {

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Only ADMIN can update reservations"
            );
        }

        validateReservationTimes(
                request.getStartTime(),
                request.getEndTime()
        );

        validatePrice(request.getPrice());

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        Resource resource =
                resourceRepository
                        .findById(request.getResourceId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found with id: "
                                                + request.getResourceId()
                                )
                        );

        if (!Boolean.TRUE.equals(resource.getAvailable())) {

            throw new IllegalStateException(
                    "Resource is currently unavailable"
            );
        }

        /*
         * Check overlap while excluding the reservation
         * currently being updated.
         */
        boolean overlappingReservation =
                reservationRepository
                        .existsOverlappingReservationExcludingId(
                                resource.getId(),
                                request.getStartTime(),
                                request.getEndTime(),
                                reservationId
                        );

        if (overlappingReservation) {

            throw new IllegalStateException(
                    "Resource is already booked for the selected time"
            );
        }

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    /**
     * Delete a reservation.
     *
     * ADMIN only.
     */
    public void deleteReservation(
            Long reservationId,
            String authenticatedEmail,
            boolean isAdmin) {

        if (!isAdmin) {
            throw new AccessDeniedException(
                    "Only ADMIN can delete reservations"
            );
        }

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        reservationRepository.delete(reservation);
    }

    /**
     * ADMIN can change reservation status.
     */
    public ReservationResponse updateReservationStatus(
            Long reservationId,
            ReservationStatus status) {

        if (status == null) {

            throw new IllegalArgumentException(
                    "Reservation status is required"
            );
        }

        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        reservation.setStatus(status);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    /**
     * Build reservation filters.
     */
    private Specification<Reservation> buildReservationSpecification(
            String authenticatedEmail,
            boolean isAdmin,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        /*
         * USER ownership restriction.
         */
        if (!isAdmin) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("user")
                                            .get("email"),
                                    authenticatedEmail
                            )
            );
        }

        /*
         * Filter by reservation status.
         */
        if (status != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        /*
         * Minimum price.
         */
        if (minPrice != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("price"),
                                    minPrice
                            )
            );
        }

        /*
         * Maximum price.
         */
        if (maxPrice != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("price"),
                                    maxPrice
                            )
            );
        }

        return specification;
    }

    /**
     * Verify that a USER can access only their own reservation.
     */
    private void validateOwnership(
            Reservation reservation,
            String authenticatedEmail,
            boolean isAdmin) {

        if (isAdmin) {
            return;
        }

        if (reservation.getUser() == null
                || reservation.getUser().getEmail() == null
                || !reservation.getUser()
                        .getEmail()
                        .equalsIgnoreCase(authenticatedEmail)) {

            throw new AccessDeniedException(
                    "You are not authorized to access this reservation"
            );
        }
    }

    /**
     * Validate reservation start/end times.
     */
    private void validateReservationTimes(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (startTime == null || endTime == null) {

            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (!startTime.isAfter(now)) {

            throw new IllegalArgumentException(
                    "Start time must be in the future"
            );
        }

        if (!endTime.isAfter(now)) {

            throw new IllegalArgumentException(
                    "End time must be in the future"
            );
        }

        if (!endTime.isAfter(startTime)) {

            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }

    /**
     * Validate reservation price.
     */
    private void validatePrice(BigDecimal price) {

        if (price == null) {

            throw new IllegalArgumentException(
                    "Price is required"
            );
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }
    }

    /**
     * Validate price filters.
     */
    private void validatePriceFilter(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be negative"
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Maximum price cannot be negative"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }
    }

    /**
     * Convert Reservation entity to response DTO.
     */
    private ReservationResponse mapToResponse(
            Reservation reservation) {

        Long userId = null;
        String userEmail = null;

        if (reservation.getUser() != null) {

            userId = reservation.getUser().getId();
            userEmail = reservation.getUser().getEmail();
        }

        Long resourceId = null;
        String resourceName = null;

        if (reservation.getResource() != null) {

            resourceId = reservation.getResource().getId();
            resourceName = reservation.getResource().getName();
        }

        return new ReservationResponse(
                reservation.getId(),
                userId,
                userEmail,
                resourceId,
                resourceName,
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}