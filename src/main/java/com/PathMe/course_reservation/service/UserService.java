package com.PathMe.course_reservation.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.LoginDTO;
import com.PathMe.course_reservation.model.DTO.MyEnrollmentResponse;
import com.PathMe.course_reservation.model.DTO.RegisterDTO;
import com.PathMe.course_reservation.model.DTO.UpdateProfileRequest;
import com.PathMe.course_reservation.model.DTO.UserProfileResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    public ResponseEntity<?> login(LoginDTO loginDTO);

    public ResponseEntity<?> register(RegisterDTO registerDTO);

    public ResponseEntity<?> logout(HttpServletResponse response);

    public ResponseEntity<?> getCurrentUser();

    public UserProfileResponse getProfile(String username);

    ResponseEntity<?> updateProfile(Authentication auth, UpdateProfileRequest request);

    void changePassword(String principalName, ChangePasswordRequest request);

    void deleteAccount(String principalName);

    void adminChangePassword(UUID userId, String newPassword);

    void adminUpdatePhoneNumber(UUID userId, String phoneNumber);

    // Enrollments
    Page<MyEnrollmentResponse> myEnrollments(String principalName, Pageable pageable);

    MyEnrollmentResponse enroll(String principalName, UUID courseId);

    void cancelEnrollment(String principalName, UUID enrollmentId);
}
