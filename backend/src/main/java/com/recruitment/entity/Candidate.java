package com.recruitment.entity;

import jakarta.persistence.*;

@Entity @Table(name="candidates")
public class Candidate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false) @JoinColumn(name="user_id", unique=true) private User user;
    private String phone; private String location; private String education;
    private Integer graduationYear;
    @Column(columnDefinition="TEXT") private String skills;
    @Column(columnDefinition="TEXT") private String experience;
    @Column(columnDefinition="TEXT") private String professionalSummary;
    private String resumePath;
    private String resumeFileName;
    private String photoPath;
    private Double latitude;
    private Double longitude;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public User getUser(){return user;} public void setUser(User user){this.user=user;}
    public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
    public String getLocation(){return location;} public void setLocation(String location){this.location=location;}
    public String getEducation(){return education;} public void setEducation(String education){this.education=education;}
    public Integer getGraduationYear(){return graduationYear;} public void setGraduationYear(Integer value){this.graduationYear=value;}
    public String getSkills(){return skills;} public void setSkills(String skills){this.skills=skills;}
    public String getExperience(){return experience;} public void setExperience(String experience){this.experience=experience;}
    public String getProfessionalSummary(){return professionalSummary;} public void setProfessionalSummary(String value){this.professionalSummary=value;}
    public String getResumePath(){return resumePath;} public void setResumePath(String resumePath){this.resumePath=resumePath;}
    public String getResumeFileName(){return resumeFileName;} public void setResumeFileName(String resumeFileName){this.resumeFileName=resumeFileName;}
    public String getPhotoPath(){return photoPath;} public void setPhotoPath(String photoPath){this.photoPath=photoPath;}
    public Double getLatitude(){return latitude;} public void setLatitude(Double latitude){this.latitude=latitude;}
    public Double getLongitude(){return longitude;} public void setLongitude(Double longitude){this.longitude=longitude;}
}
