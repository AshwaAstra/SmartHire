package com.recruitment.controller;

import com.recruitment.dto.ProfileDtos.RecruiterProfile;
import com.recruitment.entity.Recruiter;
import com.recruitment.entity.User;
import com.recruitment.repository.RecruiterRepository;
import com.recruitment.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/recruiters")
@PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
public class RecruiterController {
    private final UserRepository users;
    private final RecruiterRepository recruiters;
    public RecruiterController(UserRepository users, RecruiterRepository recruiters){this.users=users;this.recruiters=recruiters;}

    private Recruiter current(Authentication authentication){
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        return recruiters.findByUser(user).orElseGet(() -> { Recruiter r=new Recruiter(); r.setUser(user); return recruiters.save(r); });
    }
    @GetMapping("/profile")
    public RecruiterProfile profile(Authentication authentication){
        User u=users.findByEmail(authentication.getName()).orElseThrow(); Recruiter r=current(authentication);
        return new RecruiterProfile(u.getName(),u.getEmail(),"",r.getCompanyName(),r.getCompanyDescription(),r.getCompanyWebsite(),r.getCompanyLocation());
    }
    @PutMapping("/profile")
    public RecruiterProfile update(@Valid @RequestBody RecruiterProfile req, Authentication authentication){
        User u=users.findByEmail(authentication.getName()).orElseThrow(); Recruiter r=current(authentication);
        u.setName(req.name()); users.save(u);
        r.setCompanyName(req.companyName().trim()); r.setCompanyDescription(req.companyDescription());
        r.setCompanyWebsite(req.companyWebsite()); r.setCompanyLocation(req.companyLocation()); recruiters.save(r);
        return profile(authentication);
    }
}
