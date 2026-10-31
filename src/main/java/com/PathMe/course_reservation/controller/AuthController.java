package com.PathMe.course_reservation.controller;

import com.PathMe.course_reservation.model.DTO.*;
import com.PathMe.course_reservation.service.PasswordResetService;
import com.PathMe.course_reservation.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return userService.getCurrentUser();
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return userService.logout(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "A Reset Mail has been sent to your email address."
                                + " GO and check your email."
                )
        );
    }
    @PostMapping("/reset")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        this.passwordResetService.resetPassword(request);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        passwordResetService.changePassword(
                authentication.getName(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}
