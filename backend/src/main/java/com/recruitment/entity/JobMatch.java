package com.recruitment.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="job_matches")
public class JobMatch {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    @ManyToOne(optional=false) @JoinColumn(name="job_id") private Job job;
    private Integer matchScore;
    @Column(columnDefinition="TEXT") private String matchingSkills;
    @Column(columnDefinition="TEXT") private String missingSkills;
    @Column(columnDefinition="TEXT") private String recommendations;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public void setId(Long v){id=v;} public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate v){candidate=v;}
    public Job getJob(){return job;} public void setJob(Job v){job=v;} public Integer getMatchScore(){return matchScore;} public void setMatchScore(Integer v){matchScore=v;}
    public String getMatchingSkills(){return matchingSkills;} public void setMatchingSkills(String v){matchingSkills=v;} public String getMissingSkills(){return missingSkills;} public void setMissingSkills(String v){missingSkills=v;}
    public String getRecommendations(){return recommendations;} public void setRecommendations(String v){recommendations=v;} public Instant getCreatedAt(){return createdAt;}
}
