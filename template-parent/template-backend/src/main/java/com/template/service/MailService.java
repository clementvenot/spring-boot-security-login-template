package com.template.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    @Value("${app.mail.from}")
    private String from;

    public MailService(JavaMailSender mailSender, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
    }

    public void sendPasswordResetEmail(@NonNull String to,
                                       @NonNull String resetLink,
                                       @NonNull Locale locale) {
        try {
            String subject = msg("mail.reset.subject", locale);

            // Text
            String plain = """
                    %s
                    %s (%s):
                    %s

                    %s
                    """.formatted(
                    msg("mail.reset.title", locale),
                    msg("mail.reset.cta", locale),
                    msg("mail.reset.validity", locale),
                    resetLink,
                    msg("mail.reset.ignore", locale)
            );

            // HTML 
            String html = """
                <html>
                  <body style="font-family: Arial, sans-serif; line-height:1.6; color:#111;">
                    <p>%s</p>
                    <p>%s (<em>%s</em>):</p>
                    <p style="margin:20px 0;">
                      <a href="%s"
                         style="background:#4f46e5;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;display:inline-block;">
                        %s
                      </a>
                    </p>
                    <p>%s</p>
                    <p><a href="%s">%s</a></p>
                    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;"/>
                    <p style="color:#666;font-size:12px;">%s</p>
                  </body>
                </html>
                """.formatted(
                    msg("mail.reset.title", locale),
                    msg("mail.reset.cta", locale),
                    msg("mail.reset.validity", locale),
                    resetLink,
                    msg("mail.reset.cta", locale),
                    msg("mail.reset.copyLink", locale),
                    resetLink, resetLink,
                    msg("mail.reset.ignore", locale)
            );

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plain, html);

            mailSender.send(mime);
            log.info("Password reset email sent to {} with locale {}", to, locale);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        }
    }

    private String msg(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
}
