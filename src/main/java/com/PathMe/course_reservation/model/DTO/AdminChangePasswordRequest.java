package com.PathMe.course_reservation.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
