package com.PathMe.course_reservation.model.DTO;

public record AboutUsStatsResponse(
        long courseCount,
        long studentsEnrolled,
        long learningCategoriesCount
) {}