package com.recruitment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.recruitment.ai.AiService;
import com.recruitment.dto.InterviewQuestionResponse;
import com.recruitment.dto.JobMatchResponse;
import com.recruitment.dto.ResumeAnalysisResponse;
import com.recruitment.dto.ResumeImprovementResponse;
import com.recruitment.entity.*;
import com.recruitment.repository.*;
import com.recruitment.service.FileStorageService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController @RequestMapping("/api/ai") @PreAuthorize("hasRole('CANDIDATE')")
public class AiController {
    private final AiService ai; private final CandidateRepository candidates;
    private final UserRepository users; private final JobRepository jobs;
    private final ResumeAnalysisRepository analyses; private final JobMatchRepository matches;
    private final InterviewQuestionRepository interviewQuestions; private final ResumeImprovementRepository improvements;
    private final FileStorageService fileStorage;
    public AiController(AiService ai,CandidateRepository candidates,UserRepository users,JobRepository jobs,
                       ResumeAnalysisRepository analyses,JobMatchRepository matches,
                       InterviewQuestionRepository interviewQuestions,ResumeImprovementRepository improvements,
                       FileStorageService fileStorage){
        this.ai=ai;this.candidates=candidates;this.users=users;this.jobs=jobs;this.analyses=analyses;
        this.matches=matches;this.interviewQuestions=interviewQuestions;this.improvements=improvements;
        this.fileStorage=fileStorage;
    }
    private Candidate candidate(Authentication auth){return candidates.findByUser(users.findByEmail(auth.getName()).orElseThrow()).orElseThrow();}
    @PostMapping("/resume/analyze")
    public JsonNode analyze(@RequestParam(required=false) MultipartFile file,Authentication auth)throws Exception{
        Candidate c=candidate(auth); String text;
        if(file!=null){try(PDDocument doc=Loader.loadPDF(file.getBytes())){text=new PDFTextStripper().getText(doc);}
        }else if(c.getResumePath()!=null){try(PDDocument doc=Loader.loadPDF(fileStorage.read(c.getResumePath()))){text=new PDFTextStripper().getText(doc);}
        }else throw new IllegalArgumentException("Upload a PDF resume first");
        JsonNode result=ai.analyzeResume(text);
        ResumeAnalysis analysis=new ResumeAnalysis();
        analysis.setCandidate(c);
        if(result.path("resumeQualityScore").isNumber()) analysis.setResumeScore(result.path("resumeQualityScore").asInt());
        analysis.setExtractedSkills(asText(result.path("technicalSkills")));
        analysis.setStrengths(asText(result.path("strengths")));
        analysis.setWeaknesses(asText(result.path("weaknesses")));
        analysis.setSuggestions(asText(result.path("recommendedImprovements")));
        analyses.save(analysis);
        return result;
    }
    @PostMapping("/job-match/{jobId}")
    public JsonNode match(@PathVariable Long jobId,Authentication auth)throws Exception{
        Candidate c=candidate(auth); Job j=jobs.findById(jobId).orElseThrow();
        if(c.getResumePath()==null)throw new IllegalArgumentException("Upload a PDF resume first");
        try(PDDocument doc=Loader.loadPDF(fileStorage.read(c.getResumePath()))){
            String text=new PDFTextStripper().getText(doc);
            JsonNode result=ai.matchResumeWithJob(text,j.getDescription());
            JobMatch match=new JobMatch();match.setCandidate(c);match.setJob(j);
            if(result.path("overallMatchPercentage").isNumber()) match.setMatchScore(result.path("overallMatchPercentage").asInt());
            match.setMatchingSkills(asText(result.path("matchingSkills")));
            match.setMissingSkills(asText(result.path("missingSkills")));
            match.setRecommendations(asText(result.path("recommendations")));
            matches.save(match);
            return result;
        }
    }
    @PostMapping("/resume/improve")
    public JsonNode improve(@RequestParam(required=false) Long jobId,Authentication auth)throws Exception{
        Candidate c=candidate(auth); if(c.getResumePath()==null)throw new IllegalArgumentException("Upload a PDF resume first");
        Job j=jobId==null?null:jobs.findById(jobId).orElse(null);
        try(PDDocument doc=Loader.loadPDF(fileStorage.read(c.getResumePath()))){
            String text=new PDFTextStripper().getText(doc);
            JsonNode result=ai.improveResume(text,j==null?"General career improvement":j.getDescription());
            ResumeImprovement improvement=new ResumeImprovement();improvement.setCandidate(c);
            improvement.setOriginalContent(text);improvement.setImprovedContent(result.toString());
            improvements.save(improvement);
            return result;
        }
    }
    @PostMapping("/interview/generate")
    public JsonNode questions(@RequestParam Long jobId,@RequestParam(defaultValue="Mixed") String category,@RequestParam(defaultValue="Intermediate") String difficulty,@RequestParam(defaultValue="5") int count,Authentication auth)throws Exception{
        Candidate c=candidate(auth);Job j=jobs.findById(jobId).orElseThrow();
        if(count<1||count>20) throw new IllegalArgumentException("Question count must be between 1 and 20");
        String resume=c.getResumePath()==null?"Candidate skills: "+(c.getSkills()==null?"":c.getSkills()):extractResumeText(c);
        JsonNode result=ai.generateInterviewQuestions(resume,j.getDescription(),category,difficulty,count);
        JsonNode generated=result.path("questions");
        if(!generated.isArray()||generated.size()!=count)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "The local AI returned "+(generated.isArray()?generated.size():0)+" of "+count+" interview questions. Please try again.");
        for(JsonNode item:generated){
            if(item.path("question").asText("").isBlank())
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY,
                        "The local AI returned an incomplete interview question. Please try again.");
        }
        for(JsonNode item:generated){
            InterviewQuestion question=new InterviewQuestion();question.setCandidate(c);question.setJob(j);
            question.setQuestion(item.path("question").asText());
            question.setCategory(category);question.setDifficulty(difficulty);
            question.setSuggestedAnswer(item.path("suggestedAnswer").asText()+"\nFollow-ups: "+asText(item.path("followUpQuestions")));
            interviewQuestions.save(question);
        }
        return result;
    }
    @GetMapping("/interview/questions")
    public List<InterviewQuestionResponse> questionHistory(Authentication auth){
        return interviewQuestions.findByCandidate(candidate(auth)).stream().map(InterviewQuestionResponse::from).toList();
    }
    @GetMapping("/resume/analyses")
    public List<ResumeAnalysisResponse> analysisHistory(Authentication auth){
        return analyses.findByCandidate(candidate(auth)).stream().map(ResumeAnalysisResponse::from).toList();
    }
    @GetMapping("/job-matches")
    public List<JobMatchResponse> matchHistory(Authentication auth){
        return matches.findByCandidate(candidate(auth)).stream().map(JobMatchResponse::from).toList();
    }
    @GetMapping("/resume/improvements")
    public List<ResumeImprovementResponse> improvementHistory(Authentication auth){
        return improvements.findByCandidateOrderByCreatedAtDesc(candidate(auth)).stream().map(ResumeImprovementResponse::from).toList();
    }
    private String extractResumeText(Candidate candidate)throws Exception{
        try(PDDocument document=Loader.loadPDF(fileStorage.read(candidate.getResumePath()))){
            return new PDFTextStripper().getText(document);
        }
    }
    private String asText(JsonNode node){return node==null||node.isMissingNode()||node.isNull()?"":node.isTextual()?node.asText():node.toString();}
}
