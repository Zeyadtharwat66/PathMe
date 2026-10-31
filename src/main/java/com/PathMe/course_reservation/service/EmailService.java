package com.PathMe.course_reservation.service;

import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailResponse;

public interface EmailService {
    public CreateEmailResponse sendEmail(String email) throws ResendException;
    public CreateEmailResponse sendPasswordResetEmail(String username, String email, String resetUrl);

}
