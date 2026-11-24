package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name="jobs")
public class Job {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="recruiter_id") private Recruiter recruiter;
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT", nullable=false) private String description;
    @Column(columnDefinition="TEXT") private String requiredSkills;
    private String qualification; private String experience; private String location;
    private String employmentType; private String salaryRange;
    private LocalDate applicationDeadline;
    @Column(nullable=false) private boolean active=true;
    @Column(nullable=false, updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Recruiter getRecruiter(){return recruiter;} public void setRecruiter(Recruiter recruiter){this.recruiter=recruiter;}
    public String getTitle(){return title;} public void setTitle(String title){this.title=title;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public String getRequiredSkills(){return requiredSkills;} public void setRequiredSkills(String value){this.requiredSkills=value;}
    public String getQualification(){return qualification;} public void setQualification(String value){this.qualification=value;}
    public String getExperience(){return experience;} public void setExperience(String value){this.experience=value;}
    public String getLocation(){return location;} public void setLocation(String location){this.location=location;}
    public String getEmploymentType(){return employmentType;} public void setEmploymentType(String value){this.employmentType=value;}
    public String getSalaryRange(){return salaryRange;} public void setSalaryRange(String value){this.salaryRange=value;}
    public LocalDate getApplicationDeadline(){return applicationDeadline;} public void setApplicationDeadline(LocalDate value){this.applicationDeadline=value;}
    public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
    public Instant getCreatedAt(){return createdAt;}
}
