package com.PathMe.course_reservation.model.DTO;

import com.PathMe.course_reservation.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record CourseStatusRequest(
        @NotNull Status status) {
}