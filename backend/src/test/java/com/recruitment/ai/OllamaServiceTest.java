package com.recruitment.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaServiceTest {
    private final OllamaService service =
            new OllamaService("http://localhost:11434", "test", "test", "", new ObjectMapper());

    @Test
    void sendsBearerTokenWhenOllamaApiKeyIsConfigured() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/chat", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] response = "{\"message\":{\"content\":\"{\\\"ok\\\":true}\"}}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            OllamaService authenticatedService = new OllamaService(
                    "http://localhost:" + server.getAddress().getPort(),
                    "test", "test", "secret-test-key", new ObjectMapper());

            authenticatedService.matchResumeWithJob("resume", "job");

            assertEquals("Bearer secret-test-key", authorization.get());
        } finally {
            server.stop(0);
        }
    }

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
