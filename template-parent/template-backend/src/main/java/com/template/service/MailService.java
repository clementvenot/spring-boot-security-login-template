package com.template.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.reset.subject}")
    private String resetSubject;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(@NonNull String to, @NonNull String resetLink) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            // true -> multipart (text + html), UTF-8 pour les caractères spéciaux
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(resetSubject);

            // Plain text fallback
            String plain = """
                    You requested to reset your password.
                    Click the link below to set a new password (valid for a limited time):
                    %s

                    If you did not request this, you can ignore this email.
                    """.formatted(resetLink);

            // Proper HTML (no HTML entities)
            String html = """
                    <html>
                      <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #111;">
                        <p>You requested to reset your password.</p>
                        <p>
                          Click the button below to set a new password
                          (valid for a limited time):
                        </p>
                        <p style="margin: 20px 0;">
                          <a href="%s"
                             style="background: #4f46e5; color: #fff; padding: 10px 16px; text-decoration: none;
                                    border-radius: 6px; display: inline-block;">
                            Reset password
                          </a>
                        </p>
                        <p>If the button does not work, copy this link into your browser:</p>
                        <p><a href="%s">%s</a></p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                        <p style="color: #666; font-size: 12px;">
                          If you did not request this, you can ignore this email.
                        </p>
                      </body>
                    </html>
                    """.formatted(resetLink, resetLink, resetLink);

            // Envoie multipart (texte + HTML). Le 2e argument est considéré comme HTML.
            helper.setText(plain, html);
            mailSender.send(msg);

            log.info("Password reset email sent to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
            // Ici on log et on n’arrête pas le flux forgot-password côté API (anti-enumération).
            // Tu peux choisir de remonter une exception si tu veux un monitoring plus strict.
        }
    }
}