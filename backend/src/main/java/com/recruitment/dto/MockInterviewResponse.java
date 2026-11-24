package com.recruitment.dto;

import com.recruitment.entity.MockInterview;

import java.time.Instant;

public record MockInterviewResponse(Long id, String jobTitle, String interviewType, String category,
                                    String difficulty, Integer totalQuestions, Integer overallScore,
                                    Instant createdAt) {
    public static MockInterviewResponse from(MockInterview interview) {
        return new MockInterviewResponse(interview.getId(),
                interview.getJob() == null ? null : interview.getJob().getTitle(),
                interview.getInterviewType(), interview.getCategory(), interview.getDifficulty(),
                interview.getTotalQuestions(), interview.getOverallScore(), interview.getCreatedAt());
    }
}
