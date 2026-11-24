package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="mock_interviews")
public class MockInterview {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    @ManyToOne @JoinColumn(name="job_id") private Job job;
    private String interviewType;
    private String category;
    private String difficulty;
    private Integer totalQuestions;
    private Integer overallScore;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate c){candidate=c;}
    public Job getJob(){return job;} public void setJob(Job j){job=j;} public String getInterviewType(){return interviewType;} public void setInterviewType(String v){interviewType=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getDifficulty(){return difficulty;} public void setDifficulty(String v){difficulty=v;}
    public Integer getTotalQuestions(){return totalQuestions;} public void setTotalQuestions(Integer v){totalQuestions=v;} public Integer getOverallScore(){return overallScore;} public void setOverallScore(Integer v){overallScore=v;}
    public Instant getCreatedAt(){return createdAt;}
}
