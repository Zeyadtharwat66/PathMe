package com.PathMe.course_reservation.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.DTO.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final org.slf4j.Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // 1. Validation errors: @NotNull, @Email, @Size, etc.
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()));

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed",
                                request,
                                errors);
        }

        // 2. Invalid login credentials
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                errors.put("BadCredentials", "Invalid username or password");
                return buildResponse(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password",
                                request,
                                errors);
        }

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(
                        org.springframework.security.access.AccessDeniedException ex,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.FORBIDDEN,
                                "You do not have permission to access this resource",
                                request,
                                null);
        }

        @ExceptionHandler(com.PathMe.course_reservation.exception.InvalidPasswordResetTokenException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetToken(
                        com.PathMe.course_reservation.exception.InvalidPasswordResetTokenException ex,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                ex.getMessage(),
                                request,
                                null);
        }

        // 3. Explicit HTTP errors thrown by your code
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiErrorResponse> handleStatusException(
                        ResponseStatusException ex,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.valueOf(
                                ex.getStatusCode().value());

                return buildResponse(
                                status,
                                ex.getReason() != null
                                                ? ex.getReason()
                                                : status.getReasonPhrase(),
                                request,
                                null);
        }

        // 4. Any other exception
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneralException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unhandled exception at {}", request.getRequestURI(), ex);

                return buildResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "An unexpected error occurred",
                                request,
                                null);
        }

        // 5. One method to build every error response
        private ResponseEntity<ApiErrorResponse> buildResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request,
                        Map<String, String> errors) {

                ApiErrorResponse response = new ApiErrorResponse(
                                status.value(),
                                message,
                                request.getRequestURI(),
                                errors);

                return ResponseEntity.status(status).body(response);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {

                Map<String, String> errors = new HashMap<>();
                Throwable cause = ex.getMostSpecificCause();

                if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {

                        String fieldName = invalidEx.getPath().stream()
                                        .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                                        .filter(java.util.Objects::nonNull)
                                        .reduce((first, second) -> second)
                                        .orElse("requestBody");

                        Class<?> targetType = invalidEx.getTargetType();

                        if (targetType.isEnum()) {
                                String allowedValues = java.util.Arrays.stream(targetType.getEnumConstants())
                                                .map(Object::toString)
                                                .collect(java.util.stream.Collectors.joining(", "));

                                errors.put(fieldName, "Invalid value. Allowed values: " + allowedValues);
                        } else {
                                errors.put(fieldName, "Invalid value for " + targetType.getSimpleName());
                        }

                } else {
                        errors.put("requestBody", "Invalid JSON format or value");
                }

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "Invalid request body or field value",
                                request,
                                errors);
        }

}