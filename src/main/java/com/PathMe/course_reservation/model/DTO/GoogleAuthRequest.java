package com.PathMe.course_reservation.model.DTO;
import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Google ID token is required")
        String idToken,

        @NotBlank(message = "Role is required")
        String role
) {
}
