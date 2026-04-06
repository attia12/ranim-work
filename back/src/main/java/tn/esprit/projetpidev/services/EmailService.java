package tn.esprit.projetpidev.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@campway.dev}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ── Module: Official Campsite & Booking | Layer: Service (email extension) ──

    public void sendBookingConfirmationEmail(String toEmail, String camperName,
                                              String campsiteName,
                                              LocalDate checkIn,
                                              LocalDate checkOut,
                                              BigDecimal totalPrice) {
        Context ctx = new Context();
        ctx.setVariable("camperName",  camperName);
        ctx.setVariable("campsiteName", campsiteName);
        ctx.setVariable("checkIn",     checkIn);
        ctx.setVariable("checkOut",    checkOut);
        ctx.setVariable("totalPrice",  totalPrice != null ? totalPrice + " TND" : "FREE");
        ctx.setVariable("frontendUrl", frontendUrl);

        sendHtmlEmail(toEmail,
                "Booking Confirmed – " + campsiteName,
                "booking-confirmation",
                ctx);
    }

    // ── Module: Outdoor Campsite & Booking | Layer: Service (email extension) ──

    public void sendOutdoorCampsiteApprovalEmail(String toEmail, String proposerName,
                                                  String campsiteName, boolean approved,
                                                  String adminNote) {
        Context ctx = new Context();
        ctx.setVariable("proposerName",  proposerName);
        ctx.setVariable("campsiteName",  campsiteName);
        ctx.setVariable("approved",      approved);
        ctx.setVariable("status",        approved ? "APPROVED" : "REJECTED");
        ctx.setVariable("adminNote",     adminNote);
        ctx.setVariable("frontendUrl",   frontendUrl);

        sendHtmlEmail(toEmail,
                "Outdoor Campsite Proposal " + (approved ? "Approved" : "Rejected") + " – " + campsiteName,
                "outdoor-approval",
                ctx);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        Context ctx = new Context();
        ctx.setVariable("resetLink",   resetLink);
        ctx.setVariable("frontendUrl", frontendUrl);

        sendHtmlEmail(toEmail, "Password Reset Request – Campway", "password-reset", ctx);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String template, Context ctx) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            String html = templateEngine.process(template, ctx);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (MessagingException e) {
            log.warn("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
            throw new RuntimeException("Email send failed", e);
        }
    }
}
