package com.recruitment.controller;

import com.recruitment.ai.AiService;
import com.recruitment.ai.LocalTranscriptionService;
import com.recruitment.dto.InterviewAnswerResponse;
import com.recruitment.dto.MockInterviewReport;
import com.recruitment.dto.MockInterviewResponse;
import com.recruitment.entity.*;
import com.recruitment.repository.*;
import com.recruitment.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/mock-interviews") @PreAuthorize("hasRole('CANDIDATE')")
public class MockInterviewController {
    private final MockInterviewRepository interviews; private final InterviewResponseRepository responses;
    private final CandidateRepository candidates; private final UserRepository users;
    private final JobRepository jobs; private final AiService ai; private final LocalTranscriptionService transcription;
    private final FileStorageService fileStorage;
    public MockInterviewController(MockInterviewRepository interviews,InterviewResponseRepository responses,CandidateRepository candidates,UserRepository users,JobRepository jobs,AiService ai,LocalTranscriptionService transcription,FileStorageService fileStorage){this.interviews=interviews;this.responses=responses;this.candidates=candidates;this.users=users;this.jobs=jobs;this.ai=ai;this.transcription=transcription;this.fileStorage=fileStorage;}
    private Candidate candidate(Authentication auth){return candidates.findByUser(users.findByEmail(auth.getName()).orElseThrow()).orElseThrow();}

    @PostMapping
    public MockInterviewResponse create(@RequestParam(required=false) Long jobId,@RequestParam(defaultValue="TEXT") String interviewType,@RequestParam(defaultValue="Mixed") String category,@RequestParam(defaultValue="Intermediate") String difficulty,@RequestParam(defaultValue="5") int totalQuestions,Authentication auth){
        if(totalQuestions<1||totalQuestions>20) throw new IllegalArgumentException("Question count must be between 1 and 20");
        MockInterview i=new MockInterview();i.setCandidate(candidate(auth));i.setJob(jobId==null?null:jobs.findById(jobId).orElse(null));i.setInterviewType(interviewType);i.setCategory(category);i.setDifficulty(difficulty);i.setTotalQuestions(totalQuestions);return MockInterviewResponse.from(interviews.save(i));
    }
    @GetMapping public List<MockInterviewResponse> history(Authentication auth){return interviews.findByCandidate(candidate(auth)).stream().map(MockInterviewResponse::from).toList();}
    @GetMapping("/{id}") public MockInterviewResponse get(@PathVariable Long id,Authentication auth){return MockInterviewResponse.from(ownedInterview(id,auth));}
    @PostMapping("/{id}/responses")
    public InterviewAnswerResponse response(@PathVariable Long id,@RequestParam String question,@RequestParam(required=false) String textAnswer,@RequestParam(required=false) String transcribedAnswer,Authentication auth)throws Exception{
        MockInterview interview=ownedInterview(id,auth);Candidate c=candidate(auth);
        String answer=textAnswer!=null?textAnswer:transcribedAnswer;
        if(answer==null||answer.isBlank()) throw new IllegalArgumentException("An answer is required");
        var evaluation=ai.evaluateInterviewAnswer(question,answer,c.getSkills()==null?"":c.getSkills(),interview.getJob()==null?"Interview role":interview.getJob().getTitle());
        InterviewResponse r=new InterviewResponse();r.setMockInterview(interview);r.setQuestion(question);r.setTextAnswer(textAnswer);r.setTranscribedAnswer(transcribedAnswer);
        r.setAiScore(evaluation.path("score").isIntegralNumber()?evaluation.path("score").asInt():null);r.setAiFeedback(evaluation.toString());responses.save(r);
        return InterviewAnswerResponse.from(r);
    }
    @PostMapping(value="/{id}/responses/audio",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public InterviewAnswerResponse audioResponse(@PathVariable Long id,@RequestParam String question,
                                                   @RequestParam("file") MultipartFile file,Authentication auth)throws Exception{
        MockInterview interview=ownedInterview(id,auth);Candidate c=candidate(auth);
        if(file.isEmpty()) throw new IllegalArgumentException("Audio recording is required");
        if(file.getSize()>20*1024*1024) throw new IllegalArgumentException("Audio recording exceeds 20 MB");
        String mimeType=file.getContentType();
        String baseMimeType=mimeType==null?null:mimeType.split(";")[0].trim();
        if(baseMimeType==null||!List.of("audio/webm","audio/ogg","audio/wav","audio/mpeg","audio/mp4").contains(baseMimeType)){
            throw new IllegalArgumentException("Use a supported audio format: WebM, Ogg, WAV, MP3, or MP4 audio");
        }
        String transcript=transcription.transcribe(file.getBytes(),baseMimeType);
        var evaluation=ai.evaluateInterviewAnswer(question,transcript,c.getSkills()==null?"":c.getSkills(),
                interview.getJob()==null?"Interview role":interview.getJob().getTitle());
        String path=fileStorage.store(file, "interviews/" + id + "/audio", "video");
        InterviewResponse response=new InterviewResponse();response.setMockInterview(interview);response.setQuestion(question);
        response.setAudioPath(path);response.setTranscribedAnswer(transcript);response.setTextAnswer(transcript);
        response.setAiScore(evaluation.path("score").isIntegralNumber()?evaluation.path("score").asInt():null);
        response.setAiFeedback(evaluation.toString());
        return InterviewAnswerResponse.from(responses.save(response));
    }
    @PostMapping(value="/{id}/upload-audio",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> audio(@PathVariable Long id,@RequestParam("file") MultipartFile file,@RequestParam(required=false) Long responseId,Authentication auth)throws Exception{return upload(id,file,"audio",responseId,auth);}
    @PostMapping(value="/{id}/upload-video",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> video(@PathVariable Long id,@RequestParam("file") MultipartFile file,@RequestParam(required=false) Long responseId,Authentication auth)throws Exception{return upload(id,file,"video",responseId,auth);}
    private Map<String,String> upload(Long id,MultipartFile file,String kind,Long responseId,Authentication auth)throws Exception{
        ownedInterview(id,auth);
        if(file.isEmpty())throw new IllegalArgumentException("Recording is required");
        long maxSize=kind.equals("video")?25L*1024*1024:20L*1024*1024;
        if(file.getSize()>maxSize)throw new IllegalArgumentException("Recording exceeds "+(kind.equals("video")?"25":"20")+" MB");
        InterviewResponse response=null;
        if(responseId!=null){
            response=responses.findById(responseId).orElseThrow();
            if(!response.getMockInterview().getId().equals(id))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Response does not belong to this interview");
        }
        String path=fileStorage.store(file, "interviews/" + id + "/" + kind, "video");
        if(response!=null){if(kind.equals("audio"))response.setAudioPath(path);else response.setVideoPath(path);responses.save(response);}
        return Map.of("path",path);
    }
    @PostMapping("/{id}/evaluate")
    public MockInterviewReport evaluate(@PathVariable Long id,Authentication auth){
        return buildReport(ownedInterview(id,auth),true);
    }
    @GetMapping("/{id}/report")
    public MockInterviewReport report(@PathVariable Long id,Authentication auth){
        return buildReport(ownedInterview(id,auth),false);
    }
    private MockInterviewReport buildReport(MockInterview interview,boolean saveScore){
        List<InterviewResponse> interviewResponses=responses.findByMockInterview(interview);
        Integer score=null;
        if(!interviewResponses.isEmpty()){
            double average=interviewResponses.stream().map(InterviewResponse::getAiScore)
                    .filter(value->value!=null).mapToInt(Integer::intValue).average().orElse(0);
            score=saveScore||interview.getOverallScore()==null
                    ?(int)Math.round(average):interview.getOverallScore();
        }
        if(saveScore){interview.setOverallScore(score);interviews.save(interview);}
        return new MockInterviewReport(interview.getTotalQuestions(),interviewResponses.size(),score,
                interviewResponses.stream().map(InterviewAnswerResponse::from).toList());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,Authentication auth)throws Exception{
        MockInterview i=ownedInterview(id,auth);
        for(InterviewResponse response:responses.findByMockInterview(i)){
            if(response.getAudioPath()!=null) fileStorage.delete(response.getAudioPath());
            if(response.getVideoPath()!=null) fileStorage.delete(response.getVideoPath());
            responses.delete(response);
        }
        interviews.delete(i);return ResponseEntity.noContent().build();
    }
    private MockInterview ownedInterview(Long id,Authentication auth){
        MockInterview interview=interviews.findById(id).orElseThrow();
        if(!interview.getCandidate().getUser().getEmail().equals(auth.getName()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"You cannot access another candidate's interview");
        return interview;
    }
}
