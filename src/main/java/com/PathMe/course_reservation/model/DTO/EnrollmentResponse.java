package com.PathMe.course_reservation.model.DTO;

import com.PathMe.course_reservation.model.enums.EnrollmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID courseId,
        String courseName,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        String phoneNumber
) {
}
