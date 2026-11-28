package com.recruitment.ai;

import com.fasterxml.jackson.databind.JsonNode;

public interface AiService {
    JsonNode analyzeResume(String resumeText);
    JsonNode matchResumeWithJob(String resumeText, String jobDescription);
    JsonNode screenApplicantWithJob(String resumeText, String jobDescription);
    JsonNode generateInterviewQuestions(String resumeText, String jobDescription, String category, String difficulty, int count);
    JsonNode improveResume(String resumeText, String jobDescription);
    JsonNode generateLatexResume(String resumeText, String jobDescription, String templateId);
    JsonNode evaluateInterviewAnswer(String question, String answer, String skills, String jobRole);
}
