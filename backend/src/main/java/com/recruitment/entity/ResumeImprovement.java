package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="resume_improvements")
public class ResumeImprovement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    @Column(columnDefinition="TEXT",nullable=false) private String originalContent;
    @Column(columnDefinition="TEXT",nullable=false) private String improvedContent;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate v){candidate=v;}
    public String getOriginalContent(){return originalContent;} public void setOriginalContent(String v){originalContent=v;}
    public String getImprovedContent(){return improvedContent;} public void setImprovedContent(String v){improvedContent=v;}
    public Instant getCreatedAt(){return createdAt;}
}
