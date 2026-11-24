package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InterviewScheduleRequest(
        @NotNull LocalDateTime scheduledAt,
        @NotBlank @Size(max=64) String timeZone,
        @NotBlank @Size(max=1000) String details,
        @Size(max=2000)
        String message) {}
