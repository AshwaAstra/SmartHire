package com.recruitment.service;

import com.recruitment.dto.InterviewScheduleRequest;
import com.recruitment.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterviewInvitationServiceTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final InterviewInvitationService service = new InterviewInvitationService(mailSender, true, "jobs@example.com");

    @Test
    void sendsInvitationWithInterviewDetails() {
        Application application = application();
        InterviewScheduleRequest request = new InterviewScheduleRequest(
                LocalDateTime.now(ZoneId.of("UTC")).plusDays(2), "UTC",
                "Video call: https://meet.example.com/room", "Please bring your portfolio.");

        assertTrue(service.send(application, request, "Recruiter Name").isEmpty());

        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertEquals("candidate@example.com", message.getValue().getTo()[0]);
        assertEquals("Interview invitation: Software Engineer", message.getValue().getSubject());
        assertTrue(message.getValue().getText().contains("at Example Co"));
        assertTrue(message.getValue().getText().contains("https://meet.example.com/room"));
        assertTrue(message.getValue().getText().contains("Please bring your portfolio."));
    }

    @Test
    void sendsShortlistNoticeWithCompanyNameAndNoScheduleClaim() {
        assertTrue(service.sendShortlistNotice(application(), "Recruiter Name").isEmpty());

        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertEquals("candidate@example.com", message.getValue().getTo()[0]);
        assertEquals("Application update: shortlisted for Software Engineer", message.getValue().getSubject());
        assertTrue(message.getValue().getText().contains("at Example Co has been shortlisted"));
        assertTrue(message.getValue().getText().contains("will contact you with interview details"));
        assertFalse(message.getValue().getText().contains("has been scheduled"));
    }

    @Test
    void rejectsPastInterviewWithoutSendingEmail() {
        InterviewScheduleRequest request = new InterviewScheduleRequest(
                LocalDateTime.now(ZoneId.of("UTC")).minusDays(1), "UTC", "Office address", null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.send(application(), request, "Recruiter Name"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void reportsWhenEmailIsNotConfigured() {
        InterviewInvitationService disabledService =
                new InterviewInvitationService(mailSender, false, "");
        InterviewScheduleRequest request = new InterviewScheduleRequest(
                LocalDateTime.now(ZoneId.of("UTC")).plusDays(1), "UTC", "Office address", null);

        assertTrue(disabledService.send(application(), request, "Recruiter Name").orElseThrow()
                .contains("SMTP is not configured"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void reportsMailFailureWithoutHidingTheSavedSchedule() {
        doThrow(new org.springframework.mail.MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));
        InterviewScheduleRequest request = new InterviewScheduleRequest(
                LocalDateTime.now(ZoneId.of("UTC")).plusDays(1), "UTC", "Office address", null);

        assertTrue(service.send(application(), request, "Recruiter Name").orElseThrow()
                .contains("Check SMTP settings"));
    }

    private Application application() {
        User candidateUser = new User();
        candidateUser.setName("Candidate Name");
        candidateUser.setEmail("candidate@example.com");
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);

        User recruiterUser = new User();
        recruiterUser.setName("Recruiter Name");
        Recruiter recruiter = new Recruiter();
        recruiter.setUser(recruiterUser);
        recruiter.setCompanyName("Example Co");

        Job job = new Job();
        job.setTitle("Software Engineer");
        job.setRecruiter(recruiter);

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        return application;
    }
}
