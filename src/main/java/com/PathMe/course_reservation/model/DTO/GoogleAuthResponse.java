package com.PathMe.course_reservation.model.DTO;
import java.util.UUID;

public record GoogleAuthResponse(
        String message,
        UUID userId,
        String email,
        String username,
        String role
) {
}
