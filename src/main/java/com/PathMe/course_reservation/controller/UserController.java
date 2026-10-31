package com.PathMe.course_reservation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.EnrollRequest;
import com.PathMe.course_reservation.model.DTO.MyEnrollmentResponse;
import com.PathMe.course_reservation.model.DTO.UpdateProfileRequest;
import com.PathMe.course_reservation.model.DTO.UserProfileResponse;
import com.PathMe.course_reservation.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.PathMe.course_reservation.model.DTO.AdminChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.AdminUpdatePhoneRequest;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    public UserProfileResponse getProfile(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PatchMapping
    public ResponseEntity<?> update(Authentication auth,
            @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(auth, req);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication auth,
            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(auth.getName(), req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enrollments")
    public Page<MyEnrollmentResponse> myEnrollments(Authentication auth,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.myEnrollments(auth.getName(), pageable);
    }

    @PostMapping("/enrollments")
    public ResponseEntity<MyEnrollmentResponse> enroll(Authentication auth,
            @Valid @RequestBody EnrollRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.enroll(auth.getName(), req.courseId()));
    }

    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<Void> cancel(Authentication auth, @PathVariable UUID id) {
        userService.cancelEnrollment(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(Authentication auth) {
        userService.deleteAccount(auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{userId}/password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> adminChangePassword(@PathVariable UUID userId,
            @Valid @RequestBody AdminChangePasswordRequest req) {
        userService.adminChangePassword(userId, req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{userId}/phone")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> adminChangePhoneNumber(@PathVariable UUID userId,
            @Valid @RequestBody AdminUpdatePhoneRequest req) {
        userService.adminUpdatePhoneNumber(userId, req.phoneNumber());
        return ResponseEntity.noContent().build();
    }

}
