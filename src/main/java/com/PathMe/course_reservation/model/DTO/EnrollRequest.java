package com.PathMe.course_reservation.model.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollRequest(@NotNull UUID courseId) {}
