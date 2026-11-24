package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity @Table(name="applications", uniqueConstraints=@UniqueConstraint(columnNames={"candidate_id","job_id"}))
public class Application {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="candidate_id") private Candidate candidate;
    @ManyToOne(optional=false) @JoinColumn(name="job_id") private Job job;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ApplicationStatus applicationStatus=ApplicationStatus.APPLIED;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private ApplicationMethod applicationMethod=ApplicationMethod.MANUAL;
    @Column(columnDefinition="TEXT") private String coverLetter;
    private String resumePath;
    private LocalDateTime interviewScheduledAt;
    private String interviewTimeZone;
    @Column(columnDefinition="TEXT") private String interviewDetails;
    private Instant shortlistEmailSentAt;
    @Column(nullable=false, updatable=false) private Instant appliedAt=Instant.now();
    public Long getId(){return id;}
    public Candidate getCandidate(){return candidate;} public void setCandidate(Candidate candidate){this.candidate=candidate;}
    public Job getJob(){return job;} public void setJob(Job job){this.job=job;}
    public ApplicationStatus getApplicationStatus(){return applicationStatus;} public void setApplicationStatus(ApplicationStatus value){this.applicationStatus=value;}
    public ApplicationMethod getApplicationMethod(){return applicationMethod;} public void setApplicationMethod(ApplicationMethod value){this.applicationMethod=value;}
    public String getCoverLetter(){return coverLetter;} public void setCoverLetter(String value){this.coverLetter=value;}
    public String getResumePath(){return resumePath;} public void setResumePath(String value){this.resumePath=value;}
    public LocalDateTime getInterviewScheduledAt(){return interviewScheduledAt;} public void setInterviewScheduledAt(LocalDateTime value){this.interviewScheduledAt=value;}
    public String getInterviewTimeZone(){return interviewTimeZone;} public void setInterviewTimeZone(String value){this.interviewTimeZone=value;}
    public String getInterviewDetails(){return interviewDetails;} public void setInterviewDetails(String value){this.interviewDetails=value;}
    public Instant getShortlistEmailSentAt(){return shortlistEmailSentAt;} public void setShortlistEmailSentAt(Instant value){this.shortlistEmailSentAt=value;}
    public Instant getAppliedAt(){return appliedAt;}
}
