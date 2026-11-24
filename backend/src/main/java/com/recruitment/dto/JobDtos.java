package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class JobDtos {
    public record JobRequest(@NotBlank String title, @NotBlank String description, String requiredSkills,
                             String qualification, String experience, String location, String employmentType,
                             String salaryRange, LocalDate applicationDeadline) {}
}
