package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.PathMe.course_reservation.model.DTO.CourseEnrollmentAdminResponse;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.entity.EnrollmentEntity;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.EnrollmentStatus;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import com.PathMe.course_reservation.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImpTest {
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepo userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentServiceImp enrollmentService;

    @Test
    void returnsEnrollmentStatusAndUserContactDetailsForCourse() {
        UUID courseId = UUID.randomUUID();
        UUID enrollmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .username("student-name")
                .email("student@example.test")
                .phoneNumber("555-0100")
                .build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .id(enrollmentId)
                .user(user)
                .status(EnrollmentStatus.NEW)
                .course(CourseEntity.builder().id(courseId).build())
                .build();
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(enrollmentRepository.findAllByCourse_Id(courseId)).thenReturn(List.of(enrollment));

        List<CourseEnrollmentAdminResponse> result = enrollmentService.getCourseEnrollments(courseId);

        assertEquals(1, result.size());
        CourseEnrollmentAdminResponse response = result.get(0);
        assertEquals(enrollmentId, response.enrollmentId());
        assertEquals(userId, response.userId());
        assertEquals("student-name", response.name());
        assertEquals("student@example.test", response.email());
        assertEquals("555-0100", response.phoneNumber());
        assertEquals(EnrollmentStatus.NEW, response.status());
        verify(enrollmentRepository).findAllByCourse_Id(courseId);
    }

    @Test
    void rejectsUnknownCourseId() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.getCourseEnrollments(courseId));
    }

    @Test
    void updatesEnrollmentStatusAndReturnsUserDetails() {
        UUID enrollmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .username("student-name")
                .email("student@example.test")
                .phoneNumber("555-0100")
                .build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .id(enrollmentId)
                .user(user)
                .status(EnrollmentStatus.NEW)
                .build();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(java.util.Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

        CourseEnrollmentAdminResponse result = enrollmentService.updateEnrollmentStatus(
                enrollmentId, EnrollmentStatus.CONTACTED);

        assertEquals(EnrollmentStatus.CONTACTED, enrollment.getStatus());
        assertEquals(EnrollmentStatus.CONTACTED, result.status());
        assertEquals(userId, result.userId());
        assertEquals("student-name", result.name());
        assertEquals("student@example.test", result.email());
        assertEquals("555-0100", result.phoneNumber());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void rejectsUnknownEnrollmentIdWhenUpdatingStatus() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.ENROLLED));
        verifyNoInteractions(courseRepository, userRepository);
    }

    @Test
    void softDeletesEnrollment() {
        UUID enrollmentId = UUID.randomUUID();
        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .id(enrollmentId)
                .build();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(java.util.Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

        enrollmentService.deleteEnrollment(enrollmentId);

        assertNotNull(enrollment.getDeletedAt());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void rejectsUnknownEnrollmentIdWhenDeleting() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.deleteEnrollment(enrollmentId));
    }

    @Test
    void rejectsUnknownCourseIdWhenGettingEnrollments() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.getCourseEnrollments(courseId));
    }

    @Test
    void rejectsUnknownEnrollmentIdWhenUpdatinggStatus() {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> enrollmentService.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.ENROLLED));
    }
}
