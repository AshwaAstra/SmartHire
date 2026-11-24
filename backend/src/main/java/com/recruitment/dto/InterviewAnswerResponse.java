package com.recruitment.dto;

import com.recruitment.entity.InterviewResponse;

import java.time.Instant;

public record InterviewAnswerResponse(Long id, String question, Integer aiScore, String aiFeedback,
                                      String textAnswer, String transcribedAnswer, Instant createdAt) {
    public static InterviewAnswerResponse from(InterviewResponse response) {
        return new InterviewAnswerResponse(response.getId(), response.getQuestion(), response.getAiScore(),
                response.getAiFeedback(), response.getTextAnswer(), response.getTranscribedAnswer(),
                response.getCreatedAt());
    }
}
