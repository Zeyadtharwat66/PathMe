package com.PathMe.course_reservation.service.imp;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.ForgotPasswordRequest;
import com.PathMe.course_reservation.model.DTO.ResetPasswordRequest;
import com.PathMe.course_reservation.model.entity.PasswordResetToken;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.exception.InvalidPasswordResetTokenException;
import com.PathMe.course_reservation.repository.PasswordResetTokenRepository;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.EmailService;
import com.PathMe.course_reservation.service.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImp implements PasswordResetService {
        private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImp.class);

    private final UserRepo userRepo;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String identifier = request.identifier().trim();

        Optional<UserEntity> userOptional;

        if (identifier.contains("@")) {
            userOptional = userRepo.findByEmailIgnoreCase(identifier);
        } else {
            userOptional = userRepo.findByUsernameIgnoreCase(identifier);
        }

        // Do not reveal whether the account exists.
        if (userOptional.isEmpty()) {
            return;
        }

        UserEntity user = userOptional.get();

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        // Save the new token before sending the email.
        saveResetToken(user, tokenHash);

        String resetUrl = frontendUrl
                + "/reset-password?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        try {
            emailService.sendPasswordResetEmail(
                    user.getUsername(),
                    user.getEmail(),
                    resetUrl
            );
        } catch (RuntimeException ex) {
            log.error("Failed to send password reset email", ex);
        }
    }
    @Override
    @Transactional
    public void saveResetToken(
            UserEntity user,
            String tokenHash
    ) {
        // Delete any existing reset token for this user.
        resetTokenRepo.deleteByUser_Id(user.getId());

        // Force Hibernate to execute the DELETE before INSERT.
        resetTokenRepo.flush();

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(
                Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)
        );
        resetToken.setUsed(false);

        resetTokenRepo.saveAndFlush(resetToken);
    }
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        String tokenHash = hashToken(request.token());

        PasswordResetToken resetToken = resetTokenRepo
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new InvalidPasswordResetTokenException(
                                "Invalid or expired reset link"
                        )
                );

        if (resetToken.isUsed()
                || !resetToken.getExpiresAt().isAfter(Instant.now())) {

            throw new InvalidPasswordResetTokenException(
                    "Invalid or expired reset link"
            );
        }

        UserEntity user = resetToken.getUser();

        // Apply the same password rules used during registration here.
        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepo.save(user);

        // The token cannot be reused after a successful reset.
        resetTokenRepo.delete(resetToken);
    }
    @Override
    @Transactional
    public void changePassword(
            String username,
            ChangePasswordRequest request) {
        UserEntity user = userRepo.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword())) {
            throw new BadCredentialsException(
                    "Current password is incorrect");
        }
        user.setPassword(
                passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
    }

    private String generateToken() {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return java.util.HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex
            );
        }
    }
}
