package com.recruitment.controller;

import com.recruitment.dto.AuthDtos.*;
import com.recruitment.entity.Candidate;
import com.recruitment.entity.Recruiter;
import com.recruitment.entity.Role;
import com.recruitment.entity.User;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.RecruiterRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final CandidateRepository candidates;
    private final RecruiterRepository recruiters;
    private final JwtService jwt;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users, CandidateRepository candidates,
                          RecruiterRepository recruiters, JwtService jwt) {
        this.users=users; this.candidates=candidates; this.recruiters=recruiters; this.jwt=jwt;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if (users.findByEmail(request.email()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        Role role = request.role() == null ? Role.CANDIDATE : request.role();
        if (role == Role.ADMIN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADMIN registration is not allowed");
        if (role == Role.RECRUITER && (request.companyName() == null || request.companyName().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company name is required for recruiter registration");
        User user=new User();
        user.setName(request.name()); user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password())); user.setRole(role);
        user=users.save(user);
        if (role == Role.CANDIDATE) { Candidate candidate=new Candidate(); candidate.setUser(user); candidates.save(candidate); }
        if (role == Role.RECRUITER) {
            Recruiter recruiter=new Recruiter();
            recruiter.setUser(user);
            recruiter.setCompanyName(request.companyName().trim());
            recruiters.save(recruiter);
        }
        return new AuthResponse(jwt.generateToken(user), user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user=users.findByEmail(request.email().toLowerCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return new AuthResponse(jwt.generateToken(user), user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
