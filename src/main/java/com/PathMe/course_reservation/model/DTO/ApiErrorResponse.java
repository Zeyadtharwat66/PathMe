package com.PathMe.course_reservation.model.DTO;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String path,
        Map<String, String> errors) {
}
