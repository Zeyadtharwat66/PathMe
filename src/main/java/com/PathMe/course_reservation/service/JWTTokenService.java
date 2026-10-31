package com.PathMe.course_reservation.service;

import org.springframework.security.core.Authentication;

public interface JWTTokenService {
    public String generateJWTToken(Authentication auth);
    public Authentication parseJWT(String token);
}
