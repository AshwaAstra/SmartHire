package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileDtos {
    public record CandidateProfile(
            @NotBlank String name, @NotBlank String email, String phone, String location,
            String education, Integer graduationYear, String skills, String experience,
            String professionalSummary, String resumePath, String photoPath, String resumeFileName,
            Double latitude, Double longitude) {}
    public record RecruiterProfile(
            @NotBlank String name, @NotBlank String email, String phone,
            @NotBlank @Size(max=150) String companyName, String companyDescription,
            String companyWebsite, String companyLocation, String photoPath) {}
}
