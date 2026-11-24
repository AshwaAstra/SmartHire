package com.recruitment.dto;

import com.recruitment.entity.ResumeImprovement;
import java.time.Instant;

public record ResumeImprovementResponse(Long id, String originalContent, String improvedContent, Instant createdAt) {
    public static ResumeImprovementResponse from(ResumeImprovement improvement) {
        return new ResumeImprovementResponse(improvement.getId(), improvement.getOriginalContent(),
                improvement.getImprovedContent(), improvement.getCreatedAt());
    }
}
