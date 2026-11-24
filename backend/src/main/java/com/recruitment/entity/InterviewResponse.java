package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="interview_responses")
public class InterviewResponse {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="mock_interview_id") private MockInterview mockInterview;
    private String question;
    private String audioPath;
    private String videoPath;
    @Column(columnDefinition="TEXT") private String textAnswer;
    @Column(columnDefinition="TEXT") private String transcribedAnswer;
    private Integer aiScore;
    @Column(columnDefinition="TEXT") private String aiFeedback;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public MockInterview getMockInterview(){return mockInterview;} public void setMockInterview(MockInterview v){mockInterview=v;}
    public String getQuestion(){return question;} public void setQuestion(String v){question=v;} public String getAudioPath(){return audioPath;} public void setAudioPath(String v){audioPath=v;}
    public String getVideoPath(){return videoPath;} public void setVideoPath(String v){videoPath=v;} public String getTextAnswer(){return textAnswer;} public void setTextAnswer(String v){textAnswer=v;}
    public String getTranscribedAnswer(){return transcribedAnswer;} public void setTranscribedAnswer(String v){transcribedAnswer=v;} public Integer getAiScore(){return aiScore;} public void setAiScore(Integer v){aiScore=v;}
    public String getAiFeedback(){return aiFeedback;} public void setAiFeedback(String v){aiFeedback=v;} public Instant getCreatedAt(){return createdAt;}
}
