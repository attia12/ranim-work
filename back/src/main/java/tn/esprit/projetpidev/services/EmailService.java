package tn.esprit.projetpidev.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private record SmtpProfile(String host, int port) {}

    private static final Map<String, SmtpProfile> SMTP_PROFILES = Map.of(
        "gmail.com",      new SmtpProfile("smtp.gmail.com",      587),
        "googlemail.com", new SmtpProfile("smtp.gmail.com",      587),
        "outlook.com",    new SmtpProfile("smtp.office365.com",  587),
        "hotmail.com",    new SmtpProfile("smtp.office365.com",  587),
        "live.com",       new SmtpProfile("smtp.office365.com",  587),
        "yahoo.com",      new SmtpProfile("smtp.mail.yahoo.com", 587),
        "yahoo.fr",       new SmtpProfile("smtp.mail.yahoo.com", 587),
        "icloud.com",     new SmtpProfile("smtp.mail.me.com",    587),
        "me.com",         new SmtpProfile("smtp.mail.me.com",    587)
    );

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${spring.mail.password}")
    private String senderPassword;

    @Value("${spring.mail.host:}")
    private String overrideHost;

    @Value("${spring.mail.port:587}")
    private int overridePort;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ── Module: Official Campsite & Booking | Layer: Service (email extension) ──

    public void sendBookingConfirmationEmail(String toEmail, String camperName,
                                              String campsiteName,
                                              java.time.LocalDate checkIn,
                                              java.time.LocalDate checkOut,
                                              java.math.BigDecimal totalPrice) {
        String body = "Hello " + camperName + ",\n\n"
                + "Your booking at \"" + campsiteName + "\" has been CONFIRMED!\n\n"
                + "Check-in  : " + checkIn + "\n"
                + "Check-out : " + checkOut + "\n"
                + "Total price: " + (totalPrice != null ? totalPrice + " TND" : "FREE") + "\n\n"
                + "Thank you for choosing Campway!\n\n"
                + "- The Campway Team";
        sendEmail(toEmail, "Booking Confirmed – " + campsiteName, body);
    }

    // ── Module: Outdoor Campsite & Booking | Layer: Service (email extension) ──

    public void sendOutdoorCampsiteApprovalEmail(String toEmail, String proposerName,
                                                  String campsiteName, boolean approved,
                                                  String adminNote) {
        String status = approved ? "APPROVED" : "REJECTED";
        String body = "Hello " + proposerName + ",\n\n"
                + "Your outdoor campsite proposal \"" + campsiteName + "\" has been " + status + ".\n\n"
                + (adminNote != null && !adminNote.isBlank() ? "Admin note: " + adminNote + "\n\n" : "")
                + "Thank you for contributing to Campway!\n\n"
                + "- The Campway Team";
        sendEmail(toEmail, "Outdoor Campsite Proposal " + status + " – " + campsiteName, body);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String body = "Hello,\n\n"
            + "We received a request to reset your Campway password.\n\n"
            + "Click the link below to set a new password (valid for 15 minutes):\n"
            + resetLink + "\n\n"
            + "If you did not request this, you can safely ignore this email.\n\n"
            + "- The Campway Team";
        sendEmail(toEmail, "Password Reset Request - Campway", body);
    }

    private void sendEmail(String to, String subject, String text) {
        JavaMailSenderImpl sender = buildSender();
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(senderEmail);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        sender.send(msg);
    }

    private JavaMailSenderImpl buildSender() {
        SmtpProfile profile = resolveSmtpProfile();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(profile.host());
        sender.setPort(profile.port());
        sender.setUsername(senderEmail);
        sender.setPassword(senderPassword);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol",     "smtp");
        props.put("mail.smtp.auth",              "true");
        props.put("mail.smtp.starttls.enable",   "true");
        props.put("mail.smtp.starttls.required", "true");
        return sender;
    }

    private SmtpProfile resolveSmtpProfile() {
        if (overrideHost != null && !overrideHost.isBlank()) {
            return new SmtpProfile(overrideHost, overridePort);
        }
        if (senderEmail != null && senderEmail.contains("@")) {
            String domain = senderEmail.substring(senderEmail.indexOf('@') + 1).toLowerCase();
            SmtpProfile profile = SMTP_PROFILES.get(domain);
            if (profile != null) return profile;
            throw new IllegalStateException(
                "Cannot auto-detect SMTP host for domain '" + domain + "'. "
                + "Add 'spring.mail.host=smtp.yourdomain.com' to application.properties.");
        }
        throw new IllegalStateException(
            "'spring.mail.username' is not configured in application.properties.");
    }
}
