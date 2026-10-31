package com.PathMe.course_reservation.service.imp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.PathMe.course_reservation.model.DTO.CourseEnrollmentAdminResponse;
import com.PathMe.course_reservation.model.DTO.EnrollmentResponse;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.entity.EnrollmentEntity;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.EnrollmentStatus;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.EnrollmentService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImp implements EnrollmentService {

        private final EnrollmentRepository enrollmentRepository;
        private final UserRepo userRepository;
        private final CourseRepository courseRepository;

        @Transactional
        public EnrollmentResponse enroll(String username, UUID courseId) {

                // Get the logged-in user
                UserEntity user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Get the course
                CourseEntity course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

                // Prevent duplicate enrollment
                if (enrollmentRepository.existsByUser_IdAndCourse_Id(
                                user.getId(), course.getId())) {

                        throw new IllegalStateException(
                                        "You are already enrolled in this course");
                }

                // Create enrollment
                EnrollmentEntity enrollment = EnrollmentEntity.builder()
                                .user(user)
                                .course(course)
                                .status(EnrollmentStatus.NEW)
                                .build();

                EnrollmentEntity saved = enrollmentRepository.save(enrollment);

                return new EnrollmentResponse(
                                saved.getId(),
                                course.getId(),
                                course.getName(),
                                saved.getStatus(),
                                saved.getCreatedAt(),
                                userRepository.findPhoneNumberByUserId());
        }

        @Transactional
        public List<CourseEnrollmentAdminResponse> getCourseEnrollments(UUID courseId) {
                if (!courseRepository.existsById(courseId)) {
                        throw new EntityNotFoundException("Course not found");
                }

                return enrollmentRepository.findAllByCourse_Id(courseId).stream()
                                .map(enrollment -> new CourseEnrollmentAdminResponse(
                                                enrollment.getId(),
                                                enrollment.getUser().getId(),
                                                enrollment.getUser().getUsername(),
                                                enrollment.getUser().getEmail(),
                                                enrollment.getUser().getPhoneNumber(),
                                                enrollment.getStatus()))
                                .toList();
        }

        @Transactional
        public CourseEnrollmentAdminResponse updateEnrollmentStatus(UUID enrollmentId, EnrollmentStatus status) {
                EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
                enrollment.setStatus(status);
                EnrollmentEntity updated = enrollmentRepository.save(enrollment);

                return new CourseEnrollmentAdminResponse(
                                updated.getId(),
                                updated.getUser().getId(),
                                updated.getUser().getUsername(),
                                updated.getUser().getEmail(),
                                updated.getUser().getPhoneNumber(),
                                updated.getStatus());
        }

        @Transactional
        public void deleteEnrollment(UUID enrollmentId) {
                EnrollmentEntity enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
                enrollment.setDeletedAt(LocalDateTime.now());
                enrollmentRepository.save(enrollment);
        }
}