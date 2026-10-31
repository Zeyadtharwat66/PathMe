package com.PathMe.course_reservation.config;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.PathMe.course_reservation.model.enums.Role;
import com.PathMe.course_reservation.repository.UserRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomGoogleUserService extends OidcUserService {

    private final UserRepo userRepo;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest request)
            throws OAuth2AuthenticationException {

        OidcUser googleUser = super.loadUser(request);

        String email = googleUser.getEmail();
        Boolean verified = googleUser.getAttribute("email_verified");

        if (email == null || email.isBlank()
                || !Boolean.TRUE.equals(verified)) {
            throw new OAuth2AuthenticationException(
                    "Google account must have a verified email"
            );
        }

        if (userRepo.findByEmailIgnoreCase(email).isEmpty()) {
            UserEntity user = UserEntity.builder()
                    .email(email)
                    .username(email)
                    .role(Role.STUDENT)
                    .password(null)
                    .build();

            userRepo.save(user);
        }

        return googleUser;
    }
}