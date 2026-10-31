package com.PathMe.course_reservation.model.DTO;

import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Level;

import com.PathMe.course_reservation.model.enums.Status;
import jakarta.validation.constraints.*;

public record CourseRequest(
                @NotBlank @Size(max = 200) String name,
                @NotBlank String description,
                String descriptionAr,
                @NotNull @PositiveOrZero Double price,
                @PositiveOrZero Double discount,
                @NotNull @Size(min = 1) List<@NotBlank String> whatYouWillLearn,
                @NotBlank String duration,
                @NotNull Level level,
                @NotNull UUID categoryId,
                Status status) {
}