package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.ForgotPasswordRequest;
import com.PathMe.course_reservation.model.DTO.ResetPasswordRequest;
import com.PathMe.course_reservation.model.entity.PasswordResetToken;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.repository.PasswordResetTokenRepository;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.EmailService;

import com.PathMe.course_reservation.exception.InvalidPasswordResetTokenException;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImpTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordResetTokenRepository resetTokenRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetServiceImp passwordResetService;

    @BeforeEach
    void configureService() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(passwordResetService, "expirationMinutes", 15L);
    }

    @Test
    void forgotPasswordCreatesResetTokenAndSendsEmail() {
        UserEntity user = user();
        when(userRepo.findByUsernameIgnoreCase("student")).thenReturn(Optional.of(user));

        passwordResetService.forgotPassword(new ForgotPasswordRequest("student"));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(emailService).sendPasswordResetEmail(eq("student"), eq("student@example.com"), urlCaptor.capture());
        verify(resetTokenRepo).deleteByUser_Id(user.getId());
        verify(resetTokenRepo).saveAndFlush(tokenCaptor.capture());

        String resetUrl = urlCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(
                resetUrl.startsWith("http://localhost:3000/reset-password?token="));

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());
        Duration remaining = Duration.between(Instant.now(), savedToken.getExpiresAt());
        org.junit.jupiter.api.Assertions.assertTrue(remaining.toSeconds() >= 14 * 60);
        org.junit.jupiter.api.Assertions.assertTrue(remaining.toSeconds() <= 15 * 60);
    }

    @Test
    void forgotPasswordDoesNothingWhenUserDoesNotExist() {
        when(userRepo.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> passwordResetService.forgotPassword(new ForgotPasswordRequest("ghost")));

        verifyNoInteractions(emailService);
        verifyNoInteractions(resetTokenRepo);
    }

    @Test
    void forgotPasswordKeepsTokenWhenEmailSendFails() {
        UserEntity user = user();
        when(userRepo.findByUsernameIgnoreCase("student")).thenReturn(Optional.of(user));
        doThrow(new IllegalStateException("mail unavailable")).when(emailService)
                .sendPasswordResetEmail(eq("student"), eq("student@example.com"), anyString());

        assertDoesNotThrow(() -> passwordResetService.forgotPassword(new ForgotPasswordRequest("student")));

        verify(resetTokenRepo).deleteByUser_Id(user.getId());
        verify(resetTokenRepo).flush();
        verify(resetTokenRepo).saveAndFlush(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiresAt(Instant.now().minusSeconds(60));
        token.setTokenHash(hashToken("raw-token"));
        when(resetTokenRepo.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThrows(InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(new ResetPasswordRequest("raw-token", "newPassword123")));
    }

    @Test
    void resetPasswordUpdatesPasswordAndRemovesToken() {
        UserEntity user = user();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(600));
        token.setTokenHash(hashToken("raw-token"));
        when(resetTokenRepo.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-password");

        passwordResetService.resetPassword(new ResetPasswordRequest("raw-token", "newPassword123"));

        assertEquals("encoded-password", user.getPassword());
        verify(userRepo).save(user);
        verify(resetTokenRepo).delete(token);
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        UserEntity user = user();
        user.setPassword("encoded-current");
        when(userRepo.findByUsername("student")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", "encoded-current")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> passwordResetService.changePassword("student",
                        new ChangePasswordRequest("wrong-current", "newPassword123")));
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private UserEntity user() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("student");
        user.setEmail("student@example.com");
        return user;
    }
}
