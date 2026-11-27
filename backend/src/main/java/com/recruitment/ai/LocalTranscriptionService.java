package com.recruitment.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LocalTranscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(LocalTranscriptionService.class);
    private static final long TRANSCRIPTION_TIMEOUT_SECONDS = 120;

    private final String pythonLauncher;
    private final String pythonVersion;
    private final Path script;
    private final String model;
    private final String language;
    private final ObjectMapper mapper;

    public LocalTranscriptionService(@Value("${app.speech.python-launcher}") String pythonLauncher,
                                     @Value("${app.speech.python-version}") String pythonVersion,
                                     @Value("${app.speech.script}") String script,
                                     @Value("${app.speech.model}") String model,
                                     @Value("${app.speech.language}") String language,
                                     ObjectMapper mapper) {
        this.pythonLauncher = pythonLauncher;
        this.pythonVersion = pythonVersion;
        this.script = Path.of(script).toAbsolutePath().normalize();
        this.model = model;
        this.language = language;
        this.mapper = mapper;
    }

    public String transcribe(byte[] audio, String mimeType) {
        if (audio.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audio recording is empty.");
        }
        if (!Files.isRegularFile(script)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Local speech transcription script is missing: " + script);
        }

        Path audioFile = null;
        Path outputFile = null;
        try {
            audioFile = Files.createTempFile("smarthire-audio-", extensionFor(mimeType));
            outputFile = Files.createTempFile("smarthire-speech-", ".log");
            Files.write(audioFile, audio);

            List<String> command = new ArrayList<>();
            command.add(pythonLauncher);
            if (!pythonVersion.isBlank()) {
                command.add("-" + pythonVersion);
            }
            command.addAll(List.of(
                    script.toString(),
                    "--audio", audioFile.toString(),
                    "--model", model,
                    "--language", language));
            Process process;
            try {
                process = new ProcessBuilder(command)
                        .directory(script.getParent().toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(outputFile.toFile())
                        .start();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Could not start Python. Install Python 3.10 and the local speech dependencies.", e);
            }

            boolean completed;
            try {
                completed = process.waitFor(TRANSCRIPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Local speech transcription was interrupted.", e);
            }
            if (!completed) {
                process.destroyForcibly();
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                        "Local speech transcription took too long. Please try a shorter recording.");
            }

            String output = Files.readString(outputFile, StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                String detail = output.lines().filter(line -> !line.isBlank()).reduce((first, last) -> last).orElse("");
                if (output.contains("No module named 'faster_whisper'")) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                            "Install local speech support with: py -3.10 -m pip install -r requirements-speech.txt");
                }
                logger.warn("Local transcription process exited with {}: {}", process.exitValue(), detail);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Local speech transcription failed. Check the backend log and retry.");
            }

            String jsonLine = output.lines().map(String::trim)
                    .filter(line -> line.startsWith("{") && line.endsWith("}"))
                    .reduce((first, last) -> last).orElse("");
            JsonNode result;
            try {
                result = mapper.readTree(jsonLine);
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Local speech transcription returned an invalid response.", e);
            }
            String transcript = result == null ? "" : result.path("text").asText("").trim();
            if (transcript.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No speech was detected. Please record your answer again.");
            }
            return transcript;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not prepare or read the local audio transcription files.", e);
        } finally {
            deleteTemporaryFile(outputFile);
            deleteTemporaryFile(audioFile);
        }
    }

    private String extensionFor(String mimeType) {
        return switch (mimeType) {
            case "audio/webm" -> ".webm";
            case "audio/ogg" -> ".ogg";
            case "audio/wav" -> ".wav";
            case "audio/mpeg" -> ".mp3";
            case "audio/mp4" -> ".mp4";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported audio format.");
        };
    }

    private void deleteTemporaryFile(Path file) {
        if (file == null) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Could not delete temporary transcription file {}", file, e);
        }
    }
}
