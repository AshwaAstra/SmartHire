package com.recruitment.controller;

import com.recruitment.dto.AuthDtos.RegisterRequest;
import com.recruitment.entity.Recruiter;
import com.recruitment.entity.Role;
import com.recruitment.entity.User;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.RecruiterRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerRecruiterRegistrationTest {
    private final UserRepository users = mock(UserRepository.class);
    private final RecruiterRepository recruiters = mock(RecruiterRepository.class);
    private final AuthController controller = new AuthController(users, mock(CandidateRepository.class),
            recruiters, new JwtService("01234567890123456789012345678901", 86_400_000));

    @Test
    void storesCompanyNameForRecruiterRegistration() {
        when(users.findByEmail("hr@example.com")).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        controller.register(new RegisterRequest("HR Manager", "hr@example.com", "password",
                Role.RECRUITER, "  Example Company  "));

        var savedRecruiter = org.mockito.ArgumentCaptor.forClass(Recruiter.class);
        verify(recruiters).save(savedRecruiter.capture());
        assertEquals("Example Company", savedRecruiter.getValue().getCompanyName());
    }

    @Test
    void requiresCompanyNameForRecruiterRegistration() {
        when(users.findByEmail("hr@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.register(
                new RegisterRequest("HR Manager", "hr@example.com", "password", Role.RECRUITER, "  ")));

        verify(users, never()).save(any(User.class));
    }
}
