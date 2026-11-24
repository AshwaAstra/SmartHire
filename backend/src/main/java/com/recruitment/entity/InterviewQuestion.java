package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="interview_questions")
public class InterviewQuestion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    @ManyToOne @JoinColumn(name="job_id") private Job job;
    @Column(columnDefinition="TEXT",nullable=false) private String question;
    private String category;
    private String difficulty;
    @Column(columnDefinition="TEXT") private String suggestedAnswer;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate v){candidate=v;}
    public Job getJob(){return job;} public void setJob(Job v){job=v;} public String getQuestion(){return question;} public void setQuestion(String v){question=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getDifficulty(){return difficulty;} public void setDifficulty(String v){difficulty=v;}
    public String getSuggestedAnswer(){return suggestedAnswer;} public void setSuggestedAnswer(String v){suggestedAnswer=v;} public Instant getCreatedAt(){return createdAt;}
}
