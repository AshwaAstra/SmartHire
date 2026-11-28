package com.recruitment.service;

import com.recruitment.entity.PasswordResetToken;
import com.recruitment.entity.User;
import com.recruitment.repository.PasswordResetTokenRepository;
import com.recruitment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_LIFETIME_SECONDS = 15 * 60;

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String mailFrom;
    private final String frontendUrl;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder passwordEncoder, JavaMailSender mailSender,
                                @Value("${app.mail.enabled:false}") boolean mailEnabled,
                                @Value("${app.mail.from:}") String mailFrom,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl.replaceAll("/$", "");
    }

    @Transactional
    public void requestReset(String email) {
        if (!mailEnabled || mailFrom.isBlank()) return;
        users.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            boolean recentlySent = tokens.findTopByUserOrderByCreatedAtDesc(user)
                    .map(existing -> existing.getCreatedAt().plusSeconds(60).isAfter(Instant.now()))
                    .orElse(false);
            if (!recentlySent) sendReset(user);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        checkBcryptLength(newPassword);
        String hash = sha256(rawToken);
        PasswordResetToken token = tokens.findByTokenHashAndUsedAtIsNull(hash)
                .filter(value -> value.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid or expired. Request a new one."));
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        token.setUsedAt(Instant.now());
        tokens.deleteByUser(user);
        users.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        checkBcryptLength(newPassword);
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect.");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        tokens.deleteByUser(user);
        users.save(user);
    }

    private void sendReset(User user) {
        tokens.deleteByUser(user);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken record = new PasswordResetToken();
        record.setUser(user);
        record.setTokenHash(sha256(rawToken));
        record.setExpiresAt(Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS));
        tokens.save(record);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailFrom);
        mail.setTo(user.getEmail());
        mail.setSubject("Reset your SmartHire password");
        mail.setText("Hello " + user.getName() + ",\n\nUse this one-time link to choose a new SmartHire password. It expires in 15 minutes:\n\n"
                + frontendUrl + "/reset-password?token=" + rawToken
                + "\n\nIf you did not request this, you can ignore this email.");
        try {
            mailSender.send(mail);
        } catch (MailException exception) {
            tokens.delete(record);
            log.warn("Password reset email could not be sent for account {} ({})", user.getId(), exception.getClass().getSimpleName());
        }
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void checkBcryptLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes.");
    }
}
