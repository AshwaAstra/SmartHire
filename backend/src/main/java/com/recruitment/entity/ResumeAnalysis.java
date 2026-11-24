package com.recruitment.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="resume_analysis")
public class ResumeAnalysis {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    private Integer resumeScore;
    @Column(columnDefinition="TEXT") private String extractedSkills;
    @Column(columnDefinition="TEXT") private String strengths;
    @Column(columnDefinition="TEXT") private String weaknesses;
    @Column(columnDefinition="TEXT") private String suggestions;
    @Column(nullable=false,updatable=false) private Instant analyzedAt=Instant.now();
    public Long getId(){return id;} public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate c){candidate=c;}
    public Integer getResumeScore(){return resumeScore;} public void setResumeScore(Integer v){resumeScore=v;}
    public String getExtractedSkills(){return extractedSkills;} public void setExtractedSkills(String v){extractedSkills=v;}
    public String getStrengths(){return strengths;} public void setStrengths(String v){strengths=v;}
    public String getWeaknesses(){return weaknesses;} public void setWeaknesses(String v){weaknesses=v;}
    public String getSuggestions(){return suggestions;} public void setSuggestions(String v){suggestions=v;}
    public Instant getAnalyzedAt(){return analyzedAt;}
}
