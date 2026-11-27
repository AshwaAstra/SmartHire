package com.recruitment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.OllamaService;
import com.recruitment.entity.*;
import com.recruitment.repository.*;
import com.recruitment.service.FileStorageService;
import com.recruitment.service.InterviewInvitationService;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ApplicationControllerScreeningTest {
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final JobRepository jobs = mock(JobRepository.class);
    private final InterviewInvitationService invitations =
            new InterviewInvitationService(mock(JavaMailSender.class), false, "");
    private final ScreeningAi ai = new ScreeningAi();
    private final ApplicationController controller = new ApplicationController(
            applications, mock(CandidateRepository.class), jobs, mock(UserRepository.class),
            invitations, ai, mock(FileStorageService.class));

    @Test
    void shortlistsAndNotifiesOnlyTheHighestScoringApplicant() {
        Job job = job();
        Application lowerMatch = application(job, "Lower Match", "Java");
        Application higherMatch = application(job, "Higher Match", "Java, SQL, Spring");
        ReflectionTestUtils.setField(lowerMatch, "id", 1L);
        ReflectionTestUtils.setField(higherMatch, "id", 2L);
        when(jobs.findById(7L)).thenReturn(Optional.of(job));
        when(applications.findByJob(job)).thenReturn(List.of(lowerMatch, higherMatch));
        var authentication = authentication();

        var results = controller.screenApplicants(7L, authentication);

        assertEquals(List.of("Higher Match", "Lower Match"),
                results.stream().map(result -> result.candidateName()).toList());
        assertEquals(List.of(91, 58), results.stream().map(result -> result.matchScore()).toList());
        assertEquals(ApplicationStatus.SHORTLISTED, higherMatch.getApplicationStatus());
        assertEquals(ApplicationStatus.APPLIED, lowerMatch.getApplicationStatus());
        assertTrue(results.get(0).topRanked());
        assertFalse(results.get(0).emailSent());
        assertTrue(results.get(0).emailMessage().contains("SMTP is not configured"));
        assertFalse(results.get(1).topRanked());
        verify(applications).save(higherMatch);
    }

    @Test
    void shortlistsEveryApplicantTiedForHighestScore() {
        ai.setScores(91, 91);
        Job job = job();
        Application firstApplicant = application(job, "First Applicant", "Java, SQL");
        Application secondApplicant = application(job, "Second Applicant", "Java, SQL");
        ReflectionTestUtils.setField(firstApplicant, "id", 1L);
        ReflectionTestUtils.setField(secondApplicant, "id", 2L);
        when(jobs.findById(7L)).thenReturn(Optional.of(job));
        when(applications.findByJob(job)).thenReturn(List.of(firstApplicant, secondApplicant));

        var results = controller.screenApplicants(7L, authentication());

        assertEquals(2, results.stream().filter(result -> result.topRanked()).count());
        assertEquals(ApplicationStatus.SHORTLISTED, firstApplicant.getApplicationStatus());
        assertEquals(ApplicationStatus.SHORTLISTED, secondApplicant.getApplicationStatus());
        verify(applications, times(2)).save(any(Application.class));
    }

    private static class ScreeningAi extends OllamaService {
        private int callCount;
        private int[] scores = {58, 91};

        ScreeningAi() {
            super("http://localhost:11434", "test", "test", "", new ObjectMapper());
        }

        void setScores(int... values) {
            scores = values;
            callCount = 0;
        }

        @Override
        public JsonNode screenApplicantWithJob(String resumeText, String jobDescription) {
            return assessment(scores[callCount++]);
        }

        private JsonNode assessment(int score) {
            ObjectNode result = JsonNodeFactory.instance.objectNode().put("overallMatchPercentage", score);
            result.withArray("matchingSkills").add("Java");
            result.withArray("missingSkills").add("SQL");
            return result;
        }
    }

    private Job job() {
        User recruiterUser = user("Recruiter", "recruiter@example.com");
        Recruiter recruiter = new Recruiter();
        recruiter.setUser(recruiterUser);
        Job job = new Job();
        job.setId(7L);
        job.setTitle("Backend Developer");
        job.setDescription("Build backend services.");
        job.setRequiredSkills("Java, SQL");
        job.setRecruiter(recruiter);
        return job;
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("recruiter@example.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_RECRUITER")));
    }

    private Application application(Job job, String name, String skills) {
        Candidate candidate = new Candidate();
        candidate.setUser(user(name, name.toLowerCase().replace(' ', '.') + "@example.com"));
        candidate.setSkills(skills);
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        return application;
    }

    private User user(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
