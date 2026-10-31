package com.PathMe.course_reservation.model.DTO;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String idToken) {
}
