package com.recruitment.dto;

import com.recruitment.entity.ApplicationStatus;

public record ApplicantScreeningResponse(Long applicationId, String candidateName, ApplicationStatus status,
                                         boolean resumeAttached, Integer matchScore, String matchingSkills,
                                         String missingSkills, String recommendations, String screeningBasis,
                                         boolean topRanked, boolean emailSent, String emailMessage) {}
