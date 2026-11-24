package com.recruitment.controller;

import com.recruitment.dto.JobDtos.JobRequest;
import com.recruitment.dto.JobResponse;
import com.recruitment.entity.Job;
import com.recruitment.entity.Recruiter;
import com.recruitment.repository.JobRepository;
import com.recruitment.repository.RecruiterRepository;
import com.recruitment.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController @RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobs;
    private final RecruiterRepository recruiters;
    private final UserRepository users;
    public JobController(JobRepository jobs, RecruiterRepository recruiters, UserRepository users){this.jobs=jobs;this.recruiters=recruiters;this.users=users;}

    @GetMapping
    public List<JobResponse> list(@RequestParam(required=false) String location,
                          @RequestParam(required=false) String skill,
                          @RequestParam(required=false) String title){
        return jobs.findByActiveTrue().stream()
            .filter(j -> location==null || (j.getLocation()!=null && j.getLocation().toLowerCase().contains(location.toLowerCase())))
            .filter(j -> skill==null || (j.getRequiredSkills()!=null && j.getRequiredSkills().toLowerCase().contains(skill.toLowerCase())))
            .filter(j -> title==null || j.getTitle().toLowerCase().contains(title.toLowerCase()))
            .map(JobResponse::from).toList();
    }
    @GetMapping("/{id}") public JobResponse get(@PathVariable Long id){return JobResponse.from(jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found"))); }

    @GetMapping("/mine") @PreAuthorize("hasRole('RECRUITER')")
    public List<JobResponse> mine(Authentication auth) {
        Recruiter recruiter=currentRecruiter(auth);
        return jobs.findByRecruiter(recruiter).stream().map(JobResponse::from).toList();
    }

    @PostMapping @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public JobResponse create(@Valid @RequestBody JobRequest req, Authentication auth){
        Recruiter r=currentRecruiter(auth);
        Job j=new Job(); j.setRecruiter(r); copy(j,req); return JobResponse.from(jobs.save(j));
    }
    @PutMapping("/{id}") @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public JobResponse update(@PathVariable Long id,@Valid @RequestBody JobRequest req, Authentication auth){
        Job j=ownedJob(id,auth); copy(j,req); return JobResponse.from(jobs.save(j));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,Authentication auth){jobs.delete(ownedJob(id,auth));}
    @PatchMapping("/{id}/status") @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public JobResponse status(@PathVariable Long id,@RequestParam boolean active,Authentication auth){Job j=ownedJob(id,auth);j.setActive(active);return JobResponse.from(jobs.save(j));}
    private Recruiter currentRecruiter(Authentication auth) {
        return recruiters.findByUser(users.findByEmail(auth.getName()).orElseThrow())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Recruiter profile not found"));
    }
    private Job ownedJob(Long id,Authentication auth) {
        Job job=jobs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found"));
        if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !job.getRecruiter().getUser().getEmail().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot manage another recruiter's job");
        }
        return job;
    }
    private void copy(Job j,JobRequest req){j.setTitle(req.title());j.setDescription(req.description());j.setRequiredSkills(req.requiredSkills());j.setQualification(req.qualification());j.setExperience(req.experience());j.setLocation(req.location());j.setEmploymentType(req.employmentType());j.setSalaryRange(req.salaryRange());j.setApplicationDeadline(req.applicationDeadline());}
}
