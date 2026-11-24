package com.recruitment.dto;

import com.recruitment.entity.Job;

import java.time.Instant;
import java.time.LocalDate;

public record JobResponse(
        Long id, String title, String description, String requiredSkills, String qualification,
        String experience, String location, String employmentType, String salaryRange,
        LocalDate applicationDeadline, boolean active, Instant createdAt, String companyName) {
    public static JobResponse from(Job job) {
        return new JobResponse(job.getId(), job.getTitle(), job.getDescription(), job.getRequiredSkills(),
                job.getQualification(), job.getExperience(), job.getLocation(), job.getEmploymentType(),
                job.getSalaryRange(), job.getApplicationDeadline(), job.isActive(), job.getCreatedAt(),
                job.getRecruiter().getCompanyName());
    }
}
