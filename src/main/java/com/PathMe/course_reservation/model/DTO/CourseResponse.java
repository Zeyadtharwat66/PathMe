package com.PathMe.course_reservation.model.DTO;

import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Level;
import com.PathMe.course_reservation.model.enums.Status;

public record CourseResponse(
                UUID id,
                String name,
                String description,
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