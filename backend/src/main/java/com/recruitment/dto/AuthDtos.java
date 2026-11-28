package com.recruitment.dto;

import com.recruitment.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                                  @NotBlank String password, Role role,
                                  @Size(max=150) String companyName) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min=12, max=72) String newPassword,
                                        @NotBlank String confirmPassword) {}
    public record PasswordResetRequest(@Email @NotBlank String email) {}
    public record PasswordResetConfirmRequest(@NotBlank String token,
                                              @NotBlank @Size(min=12, max=72) String newPassword,
                                              @NotBlank String confirmPassword) {}
    public record AuthResponse(String token, Long userId, String name, String email, Role role) {}
    public record MessageResponse(String message) {}
}
