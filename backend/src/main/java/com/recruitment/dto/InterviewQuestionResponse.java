package com.recruitment.dto;

import com.recruitment.entity.InterviewQuestion;
import java.time.Instant;

public record InterviewQuestionResponse(Long id, String jobTitle, String question, String category,
                                        String difficulty, String suggestedAnswer, Instant createdAt) {
    public static InterviewQuestionResponse from(InterviewQuestion question) {
        return new InterviewQuestionResponse(question.getId(),
                question.getJob() == null ? null : question.getJob().getTitle(),
                question.getQuestion(), question.getCategory(), question.getDifficulty(),
                question.getSuggestedAnswer(), question.getCreatedAt());
    }
}
