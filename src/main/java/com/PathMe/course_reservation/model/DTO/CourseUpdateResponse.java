package com.PathMe.course_reservation.model.DTO;

import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Level;
import com.PathMe.course_reservation.model.enums.Status;

public record CourseUpdateResponse(
        UUID id,
        String name,
        String description,
        String descriptionAr,
        Double price,
        Double discount,
        Double discountedPrice,
        List<String> whatYouWillLearn,
        String duration,
        String courseImg,
        Level level,
        String categoryName,
        String roadmapImg,
        Status status) {
}
