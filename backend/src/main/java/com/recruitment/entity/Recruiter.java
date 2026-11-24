package com.recruitment.entity;

import jakarta.persistence.*;

@Entity @Table(name="recruiters")
public class Recruiter {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false) @JoinColumn(name="user_id", unique=true) private User user;
    private String companyName; @Column(columnDefinition="TEXT") private String companyDescription;
    private String companyWebsite; private String companyLocation;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public User getUser(){return user;} public void setUser(User user){this.user=user;}
    public String getCompanyName(){return companyName;} public void setCompanyName(String value){this.companyName=value;}
    public String getCompanyDescription(){return companyDescription;} public void setCompanyDescription(String value){this.companyDescription=value;}
    public String getCompanyWebsite(){return companyWebsite;} public void setCompanyWebsite(String value){this.companyWebsite=value;}
    public String getCompanyLocation(){return companyLocation;} public void setCompanyLocation(String value){this.companyLocation=value;}
}
