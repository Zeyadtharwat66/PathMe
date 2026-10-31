package com.PathMe.course_reservation.model.DTO;

import jakarta.validation.constraints.Pattern;

public record AdminUpdatePhoneRequest(
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "invalid phone number") String phoneNumber) {
}
