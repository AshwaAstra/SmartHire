package com.recruitment.dto;

import com.recruitment.entity.Application;
import com.recruitment.entity.ApplicationStatus;
import com.recruitment.entity.ApplicationMethod;

import java.time.Instant;
import java.time.LocalDateTime;

public record ApplicationResponse(Long id, Long jobId, String jobTitle, String candidateName,
                                 String candidateEmail, String candidateSkills,
                                 String candidatePhone, String candidateLocation, String candidateEducation,
                                 String candidateExperience, String candidateProfessionalSummary, boolean candidatePhotoAttached,
                                 ApplicationStatus status, ApplicationMethod method, String coverLetter,
                                 boolean resumeAttached, Instant appliedAt,
                                 LocalDateTime interviewScheduledAt, String interviewTimeZone,
                                 String interviewDetails) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(application.getId(), application.getJob().getId(),
                application.getJob().getTitle(), application.getCandidate().getUser().getName(),
                application.getCandidate().getUser().getEmail(), application.getCandidate().getSkills(),
                application.getCandidate().getPhone(), application.getCandidate().getLocation(),
                application.getCandidate().getEducation(), application.getCandidate().getExperience(),
                application.getCandidate().getProfessionalSummary(), application.getCandidate().getPhotoPath() != null,
                application.getApplicationStatus(), application.getApplicationMethod(), application.getCoverLetter(),
                application.getResumePath() != null, application.getAppliedAt(),
                application.getInterviewScheduledAt(), application.getInterviewTimeZone(), application.getInterviewDetails());
    }
}
