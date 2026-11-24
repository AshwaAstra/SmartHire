package com.recruitment.dto;

import com.recruitment.entity.JobMatch;
import java.time.Instant;

public record JobMatchResponse(Long id, Long jobId, String jobTitle, Integer matchScore, String matchingSkills,
                               String missingSkills, String recommendations, Instant createdAt) {
    public static JobMatchResponse from(JobMatch match) {
        return new JobMatchResponse(match.getId(), match.getJob().getId(), match.getJob().getTitle(),
                match.getMatchScore(), match.getMatchingSkills(), match.getMissingSkills(),
                match.getRecommendations(), match.getCreatedAt());
    }
}
