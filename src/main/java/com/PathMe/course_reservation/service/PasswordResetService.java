package com.PathMe.course_reservation.service;

import com.PathMe.course_reservation.model.DTO.ChangePasswordRequest;
import com.PathMe.course_reservation.model.DTO.ForgotPasswordRequest;
import com.PathMe.course_reservation.model.DTO.ResetPasswordRequest;
import com.PathMe.course_reservation.model.entity.UserEntity;
import com.resend.services.emails.model.CreateEmailResponse;

public interface PasswordResetService {
    public void forgotPassword(ForgotPasswordRequest request);

    public void saveResetToken(UserEntity user, String tokenHash);

    public void changePassword(String username, ChangePasswordRequest request);
    public void resetPassword(ResetPasswordRequest request);
}
