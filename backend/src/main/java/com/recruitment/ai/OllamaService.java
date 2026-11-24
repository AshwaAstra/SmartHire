package com.recruitment.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {
    private final RestClient client;
    private final String baseUrl;
    private final String model;
    private final String interviewModel;
    private final ObjectMapper mapper;

    public OllamaService(@Value("${app.ollama.base-url}") String baseUrl,
                         @Value("${app.ollama.model}") String model,
                         @Value("${app.ollama.interview-model}") String interviewModel,
                         ObjectMapper mapper) {
        this.client = RestClient.create();
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.model = model;
        this.interviewModel = interviewModel;
        this.mapper = mapper;
    }

    public JsonNode analyzeResume(String resumeText) {
        String prompt = "Analyze the resume using only facts explicitly present. Never invent employers, job titles, dates, degrees, achievements, or skills. " +
                "Return one JSON object with keys professionalSummary (string), technicalSkills (object of category to string arrays), education (array), " +
                "workExperience (array), projects (array), strengths (array), weaknesses (array), missingSkills (array), resumeQualityScore (integer 0-100), " +
                "formattingSuggestions (array), recommendedImprovements (array). Keep each array to at most 2 concise items. " +
                "Use empty strings, objects, or arrays when not supported by the resume. Resume:\n"+limitText(resumeText,12_000);
        try {
            return generateJson(model, prompt, 800);
        } catch (ResponseStatusException exception) {
            if (!"Local model did not return valid JSON".equals(exception.getReason())) throw exception;
            String retryPrompt = "Return only a valid JSON object, no markdown. Use only explicit resume evidence; never invent facts. " +
                    "Required keys: professionalSummary (string), technicalSkills (string array), education (string array), " +
                    "workExperience (string array), projects (string array), strengths (string array), weaknesses (string array), " +
                    "missingSkills (string array), resumeQualityScore (integer 0-100), formattingSuggestions (string array), " +
                    "recommendedImprovements (string array). Keep each array to at most 2 short items. " +
                    "Resume:\n"+limitText(resumeText,8_000);
            return generateJson(model, retryPrompt, 650);
        }
    }

    public JsonNode matchResumeWithJob(String resumeText, String jobDescription) {
        return generateJson("Compare the resume with the job using only job-related evidence: required skills, qualifications, and relevant experience. " +
                "Do not infer or consider age, gender, ethnicity, disability, photo, name, or other protected or personal characteristics. " +
                "Do not invent qualifications or experience. Return keys: overallMatchPercentage (integer 0-100), matchingSkills, missingSkills, " +
                "relevantExperience, qualificationCompatibility, jobRequirementCompatibility, recommendations. " +
                "Resume:\n"+resumeText+"\nJob requirements:\n"+jobDescription);
    }

    public JsonNode screenApplicantWithJob(String resumeText, String jobDescription) {
        String prompt = "Assess this applicant only against job-related evidence: required skills, qualifications, and relevant experience. " +
                "Ignore names and protected or personal characteristics. Do not invent evidence. " +
                "Return exactly one JSON object with this shape: " +
                "{\"overallMatchPercentage\":0,\"matchingSkills\":[],\"missingSkills\":[],\"recommendations\":\"\"}. " +
                "The score must be an integer from 0 to 100. Keep each skills array to at most 8 short items and recommendations to one concise sentence. " +
                "Resume:\n"+resumeText+"\nJob requirements:\n"+jobDescription;
        try {
            return generateJson(model, prompt, 400);
        } catch (ResponseStatusException exception) {
            if (!"Local model did not return valid JSON".equals(exception.getReason())) throw exception;
            String retryPrompt = "Return one valid JSON object only, with keys overallMatchPercentage (integer 0-100), " +
                    "matchingSkills (short string array), missingSkills (short string array), and recommendations (one short sentence). " +
                    "Do not add prose or markdown. Assess job-related evidence only. Resume:\n"+resumeText+
                    "\nJob requirements:\n"+jobDescription;
            return generateJson(model, retryPrompt, 500);
        }
    }

    public JsonNode generateInterviewQuestions(String resumeText, String jobDescription, String category, String difficulty, int count) {
        String prompt = "Create exactly "+count+" concise interview questions for the "+category+" category at "+difficulty+" difficulty. " +
                "Use the resume and job context. Return one JSON object with a top-level questions array. " +
                "Each item must contain question (one sentence), suggestedAnswer (one concise sentence), and followUpQuestions (an array with one short question). " +
                "Do not add any other keys or explanatory text. Resume:\n"+resumeText+"\nJob:\n"+jobDescription;
        return generateJson(interviewModel, prompt, Math.min(2048, Math.max(768, count * 100)));
    }

    public JsonNode improveResume(String resumeText, String jobDescription) {
        return generateJson("Improve this resume content for the selected job. Return keys: improvedProfessionalSummary, improvedProjectDescriptions, improvedWorkExperience, improvedTechnicalSkills, grammarCorrections, missingKeywords, recommendations. Original resume:\n"+resumeText+"\nJob:\n"+jobDescription);
    }

    public JsonNode evaluateInterviewAnswer(String question, String answer, String skills, String jobRole) {
        String prompt = "Evaluate this interview answer for the role. Return keys: score, strengths, weaknesses, missingKeyPoints, " +
                "improvementSuggestions, sampleImprovedAnswer, relevance, technicalAccuracy, completeness, clarity. " +
                "Keep feedback concise and sampleImprovedAnswer under two sentences. Question:"+question+"\nAnswer:"+answer+"\nSkills:"+skills+"\nRole:"+jobRole;
        return generateJson(interviewModel, prompt, 640);
    }

    private JsonNode generateJson(String prompt) {
        return generateJson(model, prompt, null);
    }

    private JsonNode generateJson(String modelName, String prompt, Integer maxTokens) {
        Map<String, Object> body = new HashMap<>(Map.of(
                "model", modelName,
                "messages", List.of(Map.of("role", "user", "content",
                        prompt+"\nReturn only valid JSON. Do not wrap the JSON in markdown.")),
                "stream", false,
                "format", "json",
                "keep_alive", "10m",
                "options", Map.of("num_predict", maxTokens == null ? 900 : maxTokens, "temperature", 0.2)));
        String response;
        try {
            response = client.post()
                    .uri(baseUrl + "/api/chat")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Local model is not installed. Run: ollama pull "+modelName, e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Local Ollama request failed (HTTP "+e.getStatusCode().value()+")", e);
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Local Ollama is not running at "+baseUrl, e);
        }

        JsonNode root;
        try {
            root = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Local Ollama returned an invalid response payload", e);
        }
        String content = root == null ? "" : root.path("message").path("content").asText("").trim();
        return parseJsonContent(content);
    }

    JsonNode parseJsonContent(String content) {
        String candidate=content==null?"":content.trim();
        try {
            JsonNode parsed=mapper.readTree(candidate);
            if (parsed!=null && parsed.isObject()) return parsed;
        } catch (JsonProcessingException ignored) {
            // Try extracting a JSON object in case the model added a short preamble or code fence.
        }

        int start=candidate.indexOf('{');
        while (start>=0) {
            int depth=0;
            boolean inString=false;
            boolean escaped=false;
            for (int index=start;index<candidate.length();index++) {
                char character=candidate.charAt(index);
                if (inString) {
                    if (escaped) escaped=false;
                    else if (character=='\\') escaped=true;
                    else if (character=='"') inString=false;
                    continue;
                }
                if (character=='"') inString=true;
                else if (character=='{') depth++;
                else if (character=='}' && --depth==0) {
                    try {
                        JsonNode parsed=mapper.readTree(candidate.substring(start,index+1));
                        if (parsed!=null && parsed.isObject()) return parsed;
                    } catch (JsonProcessingException ignored) {
                        break;
                    }
                }
            }
            start=candidate.indexOf('{',start+1);
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Local model did not return valid JSON. Please retry; if it repeats, restart Ollama and try again.");
    }

    private String limitText(String text,int maximumLength) {
        if (text==null) return "";
        return text.length()<=maximumLength?text:text.substring(0,maximumLength);
    }
}
