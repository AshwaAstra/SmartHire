package com.recruitment.dto;

public record InterviewScheduleResponse(ApplicationResponse application, boolean emailSent, String emailMessage) {}
