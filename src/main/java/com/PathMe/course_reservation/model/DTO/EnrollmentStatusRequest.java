package com.PathMe.course_reservation.model.DTO;

import com.PathMe.course_reservation.model.enums.EnrollmentStatus;

import jakarta.validation.constraints.NotNull;

public record EnrollmentStatusRequest(
        @NotNull EnrollmentStatus status) {
}