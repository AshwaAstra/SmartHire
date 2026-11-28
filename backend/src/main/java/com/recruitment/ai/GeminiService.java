package com.recruitment.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Service
@ConditionalOnProperty(name="app.ai.provider", havingValue="gemini", matchIfMissing=true)
public class GeminiService implements AiService {
    private final RestClient client;
    private final String apiKey;
    private final String model;
    private final List<String> fallbackModels;
    private final ObjectMapper mapper;

    public GeminiService(@Value("${app.gemini.api-key}") String apiKey,
                         @Value("${app.gemini.model}") String model,
                         @Value("${app.gemini.fallback-models}") String fallbackModels,
                         ObjectMapper mapper) {
        this.apiKey=apiKey; this.model=model;
        this.fallbackModels=List.of(fallbackModels.split(",")).stream().map(String::trim)
                .filter(candidate -> !candidate.isBlank()).distinct().toList();
        this.mapper=mapper;
        this.client=RestClient.create();
    }

    public JsonNode generateJson(String prompt) {
        if (apiKey == null || apiKey.isBlank())
            throw new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "GEMINI_API_KEY is not configured");
        Map<String,Object> body=Map.of(
                "model", model,
                "input", prompt+"\\nReturn only valid JSON. Do not wrap the JSON in markdown.",
                "response_format", Map.of("type", "text", "mime_type", "application/json",
                        "schema", Map.of("type", "object")));
        String json=generateContent(body, "Gemini API request");
        JsonNode root;
        try {
            root=mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Gemini returned an invalid response payload");
        }
        String text=outputText(root);
        try {
            return mapper.readTree(text);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Gemini did not return valid JSON");
        }
    }

    public String transcribeAudio(byte[] audio, String mimeType) {
        if (apiKey == null || apiKey.isBlank())
            throw new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "GEMINI_API_KEY is not configured");
        Map<String,Object> audioPart=Map.of("inline_data",Map.of(
                "mime_type",mimeType,"data",Base64.getEncoder().encodeToString(audio)));
        Map<String,Object> body=Map.of("contents",List.of(Map.of("parts",List.of(
                Map.of("text","Transcribe the spoken words in this audio accurately. Return only the transcript. Do not infer identity, emotion, accent, or other personal characteristics."),
                audioPart))));
        String json=generateContent(body, "Gemini speech transcription request");
        try {
            JsonNode root=mapper.readTree(json);
            String transcript=root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("").trim();
            if(transcript.isBlank()) throw new IllegalStateException("Gemini returned an empty transcript");
            return transcript;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Gemini could not transcribe the audio");
        }
    }

    @Override public JsonNode analyzeResume(String resumeText) {
        return generateJson("Analyze this real resume and produce keys: professionalSummary, technicalSkills, education, workExperience, projects, strengths, weaknesses, missingSkills, resumeQualityScore, formattingSuggestions, recommendedImprovements. Resume:\\n"+resumeText);
    }
    @Override public JsonNode matchResumeWithJob(String resumeText,String jobDescription) {
        return generateJson("Compare the real resume with the real job description and produce keys: overallMatchPercentage, matchingSkills, missingSkills, relevantExperience, qualificationCompatibility, jobRequirementCompatibility, recommendations. Resume:\\n"+resumeText+"\\nJob:\\n"+jobDescription);
    }
    @Override public JsonNode screenApplicantWithJob(String resumeText, String jobDescription) {
        return generateJson("Assess the applicant only against job-related skills, qualifications, and experience. Ignore names and other personal characteristics. Return exactly one JSON object with overallMatchPercentage (integer 0-100), matchingSkills (string array), missingSkills (string array), and recommendations (one concise sentence). Resume:\\n"+resumeText+"\\nJob:\\n"+jobDescription);
    }
    @Override public JsonNode generateInterviewQuestions(String resumeText,String jobDescription,String category,String difficulty,int count) {
        return generateJson("Generate "+count+" interview questions for category "+category+" at "+difficulty+" difficulty using the resume and job. Return a JSON object with questions array; each item has question, suggestedAnswer, followUpQuestions. Resume:\\n"+resumeText+"\\nJob:\\n"+jobDescription);
    }
    @Override public JsonNode improveResume(String resumeText,String jobDescription) {
        return generateJson("Improve this resume content for the selected job. Return keys: improvedProfessionalSummary, improvedProjectDescriptions, improvedWorkExperience, improvedTechnicalSkills, grammarCorrections, missingKeywords, recommendations. Original resume:\\n"+resumeText+"\\nJob:\\n"+jobDescription);
    }
    @Override public JsonNode evaluateInterviewAnswer(String question,String answer,String skills,String jobRole) {
        return generateJson("Evaluate this actual answer. Return keys: score, strengths, weaknesses, missingKeyPoints, improvementSuggestions, sampleImprovedAnswer, relevance, technicalAccuracy, completeness, clarity. Question:"+question+"\\nAnswer:"+answer+"\\nSkills:"+skills+"\\nRole:"+jobRole);
    }

    private String generateContent(Map<String,Object> body, String operation) {
        List<String> models = new ArrayList<>();
        models.add(model);
        for (String fallbackModel : fallbackModels) {
            if (!models.contains(fallbackModel)) {
                models.add(fallbackModel);
            }
        }
        for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
            String requestModel = models.get(modelIndex);
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    return client.post()
                            .uri("https://generativelanguage.googleapis.com/v1beta/interactions")
                            .header("x-goog-api-key", apiKey)
                            .header("Api-Revision", "2026-05-20")
                            .body(withModel(body, requestModel)).retrieve()
                            .body(String.class);
                } catch (RestClientResponseException e) {
                    int status = e.getStatusCode().value();
                    if ((status == 429 || status == 503) && attempt == 0) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                    "Gemini retry was interrupted", interrupted);
                        }
                        continue;
                    }
                    if (status == 503 && modelIndex + 1 < models.size()) {
                        break;
                    }
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                            operation+" failed for "+requestModel+" (HTTP "+status+"): "+providerError(e.getResponseBodyAsString()), e);
                }
            }
        }
        throw new IllegalStateException("Gemini model fallback ended unexpectedly");
    }

    private Map<String,Object> withModel(Map<String,Object> body, String requestModel) {
        Map<String,Object> request=new java.util.HashMap<>(body);
        request.put("model", requestModel);
        return request;
    }

    private String outputText(JsonNode root) {
        if (root==null) return "";
        String direct=root.path("output_text").asText(root.path("outputText").asText(""));
        if (!direct.isBlank()) return direct.trim();
        for (JsonNode step:root.path("steps")) {
            if (!"model_output".equals(step.path("type").asText())) continue;
            for (JsonNode content:step.path("content")) {
                String text=content.path("text").asText("").trim();
                if (!text.isBlank()) return text;
            }
        }
        return "";
    }

    private String providerError(String responseBody) {
        JsonNode root;
        try {
            root = mapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            return "Google returned an unreadable error response";
        }
        String message = root == null ? "" : root.path("error").path("message").asText("");
        if (message.isBlank()) {
            message = root == null ? "" : root.path("error").path("status").asText("");
        }
        if (message.isBlank()) {
            return "Google did not provide an error description";
        }
        if (apiKey != null && !apiKey.isBlank()) {
            message = message.replace(apiKey, "[redacted]");
        }
        return message.length() <= 300 ? message : message.substring(0, 300);
    }
}
