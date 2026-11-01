
package com.PathMe.course_reservation.service.imp;

import com.PathMe.course_reservation.service.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImp implements EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from}")
    private String sender;

    @Value("15")
    private long expirationMinutes;

    @Override
    public CreateEmailResponse sendEmail(String email) throws ResendException {

        Resend resend = createResendClient();

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(sender)
                .to(email)
                .subject("PathMe - Test Email")
                .html("<h2>Welcome to PathMe!</h2><p>Email service is working successfully.</p>")
                .build();

        return resend.emails().send(params);
    }

    @Override
    public CreateEmailResponse sendPasswordResetEmail(
            String username,
            String email,
            String resetUrl
    ) {

        Resend resend = createResendClient();

        String safeUsername = escapeHtml(username);
        String safeUrl = escapeHtml(resetUrl);

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Your Password</title>
            </head>

            <body style="margin:0; padding:0; font-family:Arial,sans-serif;
                         background-color:#f4f4f4;">

                <div style="max-width:600px; margin:30px auto;
                            background:#ffffff; padding:30px;
                            border-radius:10px;">

                    <h2 style="color:#333333;">Reset Your Password</h2>

                    <p>Hello %s,</p>

                    <p>
                        We received a request to reset your PathMe account password.
                        Click the button below to choose a new password.
                    </p>

                    <div style="text-align:center; margin:30px 0;">
                        <a href="%s"
                           style="background:#2563eb; color:#ffffff;
                                  padding:14px 25px; text-decoration:none;
                                  border-radius:6px; display:inline-block;">
                            Reset Password
                        </a>
                    </div>

                    <p>
                        If the button doesn't work, copy and paste this link
                        into your browser:
                    </p>

                    <p style="word-break:break-all;">
                        <a href="%s">%s</a>
                    </p>

                    <p>
                        If you didn't request a password reset,
                        you can ignore this email.
                    </p>

                    <p style="color:#777777; font-size:12px;">
                        This link expires in %s minutes.
                    </p>

                    <p>Best regards,<br>PathMe Team</p>

                </div>
            </body>
            </html>
            """;

        String html = htmlTemplate.formatted(
                safeUsername,
                safeUrl,
                safeUrl,
                safeUrl,
                expirationMinutes
        );

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(sender)
                .to(email)
                .subject("Reset your PathMe password")
                .html(html)
                .build();

        try {
            return resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private Resend createResendClient() {

        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY is not configured"
            );
        }

        return new Resend(resendApiKey);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
