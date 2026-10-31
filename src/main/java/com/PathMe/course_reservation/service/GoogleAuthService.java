package com.PathMe.course_reservation.service;

import com.PathMe.course_reservation.model.entity.UserEntity;

public interface GoogleAuthService {
    public UserEntity authenticateGoogleUser(String idToken, String requestedRole);
    public UserEntity loginWithGoogle(String idToken);
}
