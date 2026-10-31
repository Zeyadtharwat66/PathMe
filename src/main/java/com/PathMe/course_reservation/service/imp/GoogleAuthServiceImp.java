package com.PathMe.course_reservation.service.imp;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.Role;
import com.PathMe.course_reservation.repository.UserRepo;
import com.PathMe.course_reservation.service.GoogleAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Locale;
@Service
public class GoogleAuthServiceImp implements GoogleAuthService {
    private final UserRepo userRepo;
    private final GoogleIdTokenVerifier verifier;
    public GoogleAuthServiceImp(
            UserRepo userRepo,
            @Value("${app.google.client-id}") String clientId) {
        this.userRepo = userRepo;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
    @Transactional
    public UserEntity authenticateGoogleUser(
            String idToken,
            String requestedRole) {
        GoogleIdToken.Payload payload;
        try {
            GoogleIdToken verifiedToken = verifier.verify(idToken);
            if (verifiedToken == null) {
                throw new IllegalArgumentException(
                        "Invalid Google ID token");
            }
            payload = verifiedToken.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalArgumentException(
                    "Could not verify Google ID token");
        }
        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        Boolean emailVerified =
                (Boolean) payload.get("email_verified");
        if (googleSub == null || googleSub.isBlank()
                || email == null || email.isBlank()
                || !Boolean.TRUE.equals(emailVerified)) {

            throw new IllegalArgumentException(
                    "Google account must have a verified email");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        // First, find an account linked to this exact Google identity.
        var linkedUser = userRepo.findByGoogleSub(googleSub);
        if (linkedUser.isPresent()) {
            return linkedUser.get();
        }
        // Do not automatically link Google to an existing account by email.
        if (userRepo.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                    "An account with this email already exists. "
                            + "Sign in using that account or link Google securely.");
        }
        Role role;
        try {
            role = Role.valueOf(requestedRole.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid role");
        }
        if (role != Role.STUDENT && role != Role.PARENT) {
            throw new IllegalArgumentException(
                    "Only STUDENT and PARENT roles are allowed");
        }
        UserEntity newUser = UserEntity.builder()
                .username(email)
                .email(email)
                .googleSub(googleSub)
                .role(role)
                .password(null)
                .build();
        return userRepo.save(newUser);
    }
    @Override
    @Transactional(readOnly = true)
    public UserEntity loginWithGoogle(String idToken) {

        GoogleIdToken.Payload payload;

        try {
            GoogleIdToken verifiedToken = verifier.verify(idToken);

            if (verifiedToken == null) {
                throw new IllegalArgumentException(
                        "Invalid Google ID token"
                );
            }

            payload = verifiedToken.getPayload();

        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalArgumentException(
                    "Could not verify Google ID token"
            );
        }

        String googleSub = payload.getSubject();
        String email = payload.getEmail();

        Boolean emailVerified =
                (Boolean) payload.get("email_verified");

        if (googleSub == null || googleSub.isBlank()
                || email == null || email.isBlank()
                || !Boolean.TRUE.equals(emailVerified)) {

            throw new IllegalArgumentException(
                    "Google account must have a verified email"
            );
        }

        // Find the account linked to this exact Google identity.
        return userRepo.findByGoogleSub(googleSub)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No account is linked to this Google account. Please sign up first."
                        )
                );
    }
}
