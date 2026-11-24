package com.recruitment.dto;

import java.util.List;

public record MockInterviewReport(Integer totalQuestions, Integer questionsAnswered, Integer overallScore,
                                  List<InterviewAnswerResponse> responses) {}
