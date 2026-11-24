package com.recruitment.dto;

import com.recruitment.entity.ApplicationMethod;
import jakarta.validation.constraints.NotNull;

public record ApplicationSubmission(@NotNull Long jobId, @NotNull ApplicationMethod method, String coverLetter) {}
