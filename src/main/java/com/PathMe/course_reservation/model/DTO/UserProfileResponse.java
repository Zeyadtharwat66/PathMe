package com.PathMe.course_reservation.model.DTO;

import com.PathMe.course_reservation.model.enums.Gender;
import com.PathMe.course_reservation.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        String username,
        Role role,
        String email,
        String phoneNumber,
        Gender gender,
        Integer age,
        LocalDateTime createdAt
) {
}
