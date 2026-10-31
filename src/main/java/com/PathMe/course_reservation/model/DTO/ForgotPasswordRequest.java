package com.PathMe.course_reservation.model.DTO;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Username or email is required")
        @Size(max = 254)
        String identifier
) {
}
