package com.PathMe.course_reservation.controller;

import com.PathMe.course_reservation.model.DTO.GoogleAuthRequest;
import com.PathMe.course_reservation.model.DTO.GoogleAuthResponse;
import com.PathMe.course_reservation.model.DTO.GoogleLoginRequest;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.service.GoogleAuthService;
import com.PathMe.course_reservation.service.imp.GoogleAuthServiceImp;
import com.PathMe.course_reservation.config.CustomStudentDetails;
import com.PathMe.course_reservation.service.JWTTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final JWTTokenService jwtTokenService;

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponse> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletRequest httpRequest) {

        UserEntity user = googleAuthService.authenticateGoogleUser(
                request.idToken(),
                request.role()
        );

        CustomStudentDetails userDetails =
                new CustomStudentDetails(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String jwt = jwtTokenService.generateJWTToken(authentication);

        boolean secureCookie = httpRequest.isSecure();

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        GoogleAuthResponse response = new GoogleAuthResponse(
                "Google authentication successful",
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
        System.out.println(request.idToken());
        System.out.println(cookie.toString());
        System.out.println("Google login succeeded; cookie header created: "
                + (cookie != null));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
    @PostMapping("/google/login")
    public ResponseEntity<GoogleAuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {

        // Validate Google token and find an existing user.
        UserEntity user =
                googleAuthService.loginWithGoogle(request.idToken());

        CustomStudentDetails userDetails =
                new CustomStudentDetails(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String jwt =
                jwtTokenService.generateJWTToken(authentication);

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) // Local HTTP development only
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        GoogleAuthResponse response = new GoogleAuthResponse(
                "Google login successful",
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}
