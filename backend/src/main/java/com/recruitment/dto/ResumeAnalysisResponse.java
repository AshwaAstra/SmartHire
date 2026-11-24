package com.recruitment.dto;

import com.recruitment.entity.ResumeAnalysis;
import java.time.Instant;

public record ResumeAnalysisResponse(Long id, Integer resumeScore, String extractedSkills, String strengths,
                                     String weaknesses, String suggestions, Instant analyzedAt) {
    public static ResumeAnalysisResponse from(ResumeAnalysis analysis) {
        return new ResumeAnalysisResponse(analysis.getId(), analysis.getResumeScore(), analysis.getExtractedSkills(),
                analysis.getStrengths(), analysis.getWeaknesses(), analysis.getSuggestions(), analysis.getAnalyzedAt());
    }
}
