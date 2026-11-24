package com.recruitment.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaServiceTest {
    private final OllamaService service =
            new OllamaService("http://localhost:11434", "test", "test", new ObjectMapper());

    @Test
    void parsesJsonObjectWrappedInModelFormatting() {
        var result = service.parseJsonContent("```json\n{\"resumeQualityScore\":82,\"projects\":[\"Portal\"]}\n```");

        assertEquals(82, result.path("resumeQualityScore").asInt());
        assertEquals("Portal", result.path("projects").get(0).asText());
    }

    @Test
    void rejectsMalformedModelJsonInsteadOfReturningIncompleteAnalysis() {
        assertThrows(ResponseStatusException.class,
                () -> service.parseJsonContent("{\"resumeQualityScore\":82, \"projects\":[}"));
    }
}
