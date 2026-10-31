package com.PathMe.course_reservation.service;

import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.DTO.CourseEnrollmentAdminResponse;
import com.PathMe.course_reservation.model.DTO.EnrollmentResponse;

public interface EnrollmentService {
    public EnrollmentResponse enroll(String username, UUID courseId);

    List<CourseEnrollmentAdminResponse> getCourseEnrollments(UUID courseId);

    CourseEnrollmentAdminResponse updateEnrollmentStatus(UUID enrollmentId,
            com.PathMe.course_reservation.model.enums.EnrollmentStatus status);

    void deleteEnrollment(UUID enrollmentId);

}
