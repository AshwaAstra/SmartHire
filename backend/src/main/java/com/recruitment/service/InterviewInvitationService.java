package com.recruitment.service;

import com.recruitment.dto.InterviewScheduleRequest;
import com.recruitment.entity.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class InterviewInvitationService {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu 'at' h:mm a z");

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public InterviewInvitationService(JavaMailSender mailSender,
                                     @Value("${app.mail.enabled:false}") boolean enabled,
                                     @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public Optional<String> send(Application application, InterviewScheduleRequest request, String recruiterName) {
        ZoneId zone;
        try {
            zone = ZoneId.of(request.timeZone());
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid time zone.", exception);
        }
        if (!request.scheduledAt().atZone(zone).isAfter(java.time.ZonedDateTime.now(zone))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview time must be in the future.");
        }
        String candidateName = application.getCandidate().getUser().getName();
        String companyName = application.getJob().getRecruiter().getCompanyName();
        String subject = "Interview invitation: " + application.getJob().getTitle();
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(candidateName).append(",\n\n")
                .append("Your application for ").append(application.getJob().getTitle());
        if (companyName != null && !companyName.isBlank()) body.append(" at ").append(companyName);
        body.append(" has been shortlisted, and an interview has been scheduled.\n\n")
                .append("Date and time: ").append(request.scheduledAt().atZone(zone).format(DATE_FORMAT)).append("\n")
                .append("Interview details: ").append(request.details().trim()).append("\n");
        if (request.message() != null && !request.message().isBlank()) {
            body.append("\nMessage from the recruiter:\n").append(request.message().trim()).append("\n");
        }
        body.append("\nPlease reply to this email if you have any questions.\n\n")
                .append("Regards,\n").append(recruiterName);
        if (companyName != null && !companyName.isBlank()) body.append("\n").append(companyName);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(application.getCandidate().getUser().getEmail());
        email.setSubject(subject);
        email.setText(body.toString());
        return deliver(email);
    }

    public Optional<String> sendShortlistNotice(Application application, String recruiterName) {
        String companyName = application.getJob().getRecruiter().getCompanyName();
        StringBuilder body = new StringBuilder()
                .append("Hello ").append(application.getCandidate().getUser().getName()).append(",\n\n")
                .append("Your application for ").append(application.getJob().getTitle());
        if (companyName != null && !companyName.isBlank()) body.append(" at ").append(companyName);
        body.append(" has been shortlisted for the next stage. Our recruiting team will contact you with interview details.\n\n")
                .append("Regards,\n").append(recruiterName);
        if (companyName != null && !companyName.isBlank()) body.append("\n").append(companyName);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(application.getCandidate().getUser().getEmail());
        email.setSubject("Application update: shortlisted for " + application.getJob().getTitle());
        email.setText(body.toString());
        return deliver(email);
    }

    private Optional<String> deliver(SimpleMailMessage email) {
        if (!enabled || from.isBlank()) {
            return Optional.of("Email was not sent because SMTP is not configured. Set MAIL_ENABLED, MAIL_FROM, and SMTP settings to enable invitations.");
        }
        email.setFrom(from);
        try {
            mailSender.send(email);
            return Optional.empty();
        } catch (MailAuthenticationException exception) {
            return Optional.of("Gmail rejected SMTP sign-in. Check that MAIL_USERNAME is the sender Gmail address and MAIL_PASSWORD is its 16-character App Password, then restart the backend.");
        } catch (MailException exception) {
            return Optional.of("SMTP could not send the email ("+exception.getClass().getSimpleName()+"). Check SMTP settings: smtp.gmail.com, port 587, TLS, internet access, and the sender App Password.");
        }
    }
}
