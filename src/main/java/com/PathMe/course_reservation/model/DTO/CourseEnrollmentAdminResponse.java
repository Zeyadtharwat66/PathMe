package com.PathMe.course_reservation.model.DTO;

import java.util.UUID;

import com.PathMe.course_reservation.model.enums.EnrollmentStatus;

public record CourseEnrollmentAdminResponse(
        UUID enrollmentId,
        UUID userId,
        String name,
        String email,
        String phoneNumber,
        EnrollmentStatus status) {
}
