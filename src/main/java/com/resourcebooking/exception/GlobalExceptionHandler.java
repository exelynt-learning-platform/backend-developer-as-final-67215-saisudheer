package com.resourcebooking.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ==========================================
     * RESOURCE NOT FOUND
     * ==========================================
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        "Resource Not Found",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * RESERVATION NOT FOUND
     * ==========================================
     */

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(
            ReservationNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        "Reservation Not Found",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * USER NOT FOUND
     * ==========================================
     */

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        "User Not Found",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * VALIDATION ERRORS
     * ==========================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Validation Failed",
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * BAD REQUEST
     * ==========================================
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * INVALID STATE
     * ==========================================
     */

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Invalid State",
                        exception.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * BAD CREDENTIALS
     * ==========================================
     */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Invalid email or password",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * ACCESS DENIED
     * ==========================================
     */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "You do not have permission to access this resource",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * GENERAL EXCEPTION
     * ==========================================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {

        ErrorResponse errorResponse =
                createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /*
     * ==========================================
     * ERROR RESPONSE BUILDER
     * ==========================================
     */

    private ErrorResponse createErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path) {

        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path
        );
    }
}