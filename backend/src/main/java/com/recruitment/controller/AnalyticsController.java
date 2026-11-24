package com.recruitment.controller;

import com.recruitment.entity.Role;
import com.recruitment.entity.ApplicationStatus;
import com.recruitment.entity.Recruiter;
import com.recruitment.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/analytics")
public class AnalyticsController {
    private final UserRepository users; private final JobRepository jobs;
    private final ApplicationRepository applications; private final RecruiterRepository recruiters;
    private final CandidateRepository candidates; private final ResumeAnalysisRepository resumeAnalyses;
    private final MockInterviewRepository mockInterviews;
    public AnalyticsController(UserRepository users,JobRepository jobs,ApplicationRepository applications,
                               RecruiterRepository recruiters,CandidateRepository candidates,
                               ResumeAnalysisRepository resumeAnalyses,MockInterviewRepository mockInterviews){
        this.users=users;this.jobs=jobs;this.applications=applications;this.recruiters=recruiters;
        this.candidates=candidates;this.resumeAnalyses=resumeAnalyses;this.mockInterviews=mockInterviews;
    }

    @GetMapping("/admin") @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Long> admin(){
        long candidates=users.findAll().stream().filter(u->u.getRole()==Role.CANDIDATE).count();
        long recruiters=users.findAll().stream().filter(u->u.getRole()==Role.RECRUITER).count();
        return Map.of("totalCandidates",candidates,"totalRecruiters",recruiters,"totalJobs",(long)jobs.count(),"totalApplications",(long)applications.count());
    }
    @GetMapping("/recruiter") @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public Map<String,Long> recruiter(Authentication authentication){
        Recruiter recruiter=recruiters.findByUser(users.findByEmail(authentication.getName()).orElseThrow()).orElseThrow();
        var ownedJobs=jobs.findByRecruiter(recruiter);
        var ownedApplications=ownedJobs.stream().flatMap(job->applications.findByJob(job).stream()).toList();
        return Map.of("totalJobs",(long)ownedJobs.size(),"totalApplications",(long)ownedApplications.size(),
                "shortlistedCandidates",ownedApplications.stream().filter(a->a.getApplicationStatus()==ApplicationStatus.SHORTLISTED).count(),
                "rejectedCandidates",ownedApplications.stream().filter(a->a.getApplicationStatus()==ApplicationStatus.REJECTED).count());
    }
    @GetMapping("/candidate") @PreAuthorize("hasRole('CANDIDATE')")
    public Map<String,Number> candidate(Authentication authentication){
        var candidate=candidates.findByUser(users.findByEmail(authentication.getName()).orElseThrow()).orElseThrow();
        var candidateApplications=applications.findByCandidate(candidate);
        var candidateAnalyses=resumeAnalyses.findByCandidate(candidate);
        var candidateInterviews=mockInterviews.findByCandidate(candidate);
        int resumeScore=candidateAnalyses.stream().filter(a->a.getResumeScore()!=null)
                .max(java.util.Comparator.comparing(analysis->analysis.getAnalyzedAt()))
                .map(analysis->analysis.getResumeScore()).orElse(0);
        double interviewAverage=candidateInterviews.stream().filter(i->i.getOverallScore()!=null)
                .mapToInt(i->i.getOverallScore()).average().orElse(0);
        long shortlisted=candidateApplications.stream().filter(a->a.getApplicationStatus()==ApplicationStatus.SHORTLISTED).count();
        return Map.of("jobsApplied",candidateApplications.size(),"resumeAnalysisScore",resumeScore,
                "averageInterviewScore",Math.round(interviewAverage),"shortlistedApplications",shortlisted);
    }
}
