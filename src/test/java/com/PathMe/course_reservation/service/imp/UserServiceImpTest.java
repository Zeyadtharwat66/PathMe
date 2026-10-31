package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.Gender;
import com.PathMe.course_reservation.model.enums.Role;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.JWTTokenService;
import com.PathMe.course_reservation.service.imp.UserServiceImp;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepo userRepo;

    @Mock
    private JWTTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnrollmentRepository enrollmentRepo;

    @Mock
    private CourseRepository courseRepo;

    @InjectMocks
    private UserServiceImp userService;

    @Test
    void adminChangePasswordUpdatesPasswordForSelectedUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).password("old-hash").build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("NewPass123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-hash");

        userService.adminChangePassword(userId, "NewPass123");

        assertEquals("new-hash", user.getPassword());
        verify(userRepo).save(user);
    }

    @Test
    void adminChangePasswordRejectsBlankPassword() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).password("old-hash").build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.adminChangePassword(userId, "   "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void adminChangePhoneNumberUpdatesPhoneNumberForSelectedUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).phoneNumber("01000000000").build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        userService.adminUpdatePhoneNumber(userId, "+966500000000");

        assertEquals("+966500000000", user.getPhoneNumber());
        verify(userRepo).save(user);
    }

    @Test
    void adminChangePhoneNumberRejectsInvalidFormat() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).phoneNumber("01000000000").build();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.adminUpdatePhoneNumber(userId, "12345"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void enrollRejectsDuplicateCourseForSameUser() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .username("student")
                .role(Role.STUDENT)
                .email("student@example.com")
                .gender(Gender.MALE)
                .build();
        CourseEntity course = CourseEntity.builder().id(courseId).name("Spring Boot").build();

        when(userRepo.findByUsername("student")).thenReturn(Optional.of(user));
        when(courseRepo.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepo.existsByUser_IdAndCourse_Id(userId, courseId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.enroll("student", courseId));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}
