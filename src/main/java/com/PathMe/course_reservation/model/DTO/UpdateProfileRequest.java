package com.PathMe.course_reservation.model.DTO;

import com.PathMe.course_reservation.model.enums.Gender;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
                @Size(min = 3, max = 50) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String username,

                @Email @Size(max = 255) String email,

                @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "invalid phone number") String phoneNumber,

                Gender gender,

                @Min(5) @Max(120) Integer age) {
}