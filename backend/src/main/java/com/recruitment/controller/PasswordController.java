package com.recruitment.controller;

import com.recruitment.dto.AuthDtos.*;
import com.recruitment.entity.User;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtService;
import com.recruitment.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PasswordController {
    private final UserRepository users;
    private final PasswordResetService passwordResetService;
    private final JwtService jwt;

    public PasswordController(UserRepository users, PasswordResetService passwordResetService, JwtService jwt) {
        this.users = users;
        this.passwordResetService = passwordResetService;
        this.jwt = jwt;
    }

    @PostMapping("/api/account/password")
    public AuthResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match.");
        if (request.currentPassword().equals(request.newPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a new password different from your current one.");
        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found."));
        try {
            passwordResetService.changePassword(user, request.currentPassword(), request.newPassword());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        return new AuthResponse(jwt.generateToken(user), user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @PostMapping("/api/auth/password-reset/request")
    public MessageResponse requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());
        return new MessageResponse("If that email belongs to an account and email delivery is enabled, a reset link will arrive shortly.");
    }

    @PostMapping("/api/auth/password-reset/confirm")
    public MessageResponse confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match.");
        try {
            passwordResetService.resetPassword(request.token(), request.newPassword());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        return new MessageResponse("Password changed. Sign in with your new password.");
    }
}
