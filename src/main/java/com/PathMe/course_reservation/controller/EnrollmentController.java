package com.PathMe.course_reservation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.PathMe.course_reservation.model.DTO.CourseEnrollmentAdminResponse;
import com.PathMe.course_reservation.model.DTO.EnrollmentResponse;
import com.PathMe.course_reservation.model.DTO.EnrollmentStatusRequest;
import com.PathMe.course_reservation.service.EnrollmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<CourseEnrollmentAdminResponse> getCourseEnrollments(@PathVariable UUID courseId) {
        return enrollmentService.getCourseEnrollments(courseId);
    }
    @PatchMapping("/{enrollmentId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CourseEnrollmentAdminResponse updateEnrollmentStatus(
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody EnrollmentStatusRequest request) {
        return enrollmentService.updateEnrollmentStatus(enrollmentId, request.status());
    }

    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable UUID enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT')")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable UUID courseId,
            Authentication authentication) {
        EnrollmentResponse response = enrollmentService.enroll(
                authentication.getName(),
                courseId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}