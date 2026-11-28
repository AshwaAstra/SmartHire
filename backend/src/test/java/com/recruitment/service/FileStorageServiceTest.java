package com.recruitment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void storesReadsAndDeletesFilesInLocalMode() throws Exception {
        FileStorageService storage = new FileStorageService("local", tempDir.toString(), "", "", "");
        var file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{1, 2, 3});

        String reference = storage.store(file, "resumes", "raw");

        assertArrayEquals(new byte[]{1, 2, 3}, storage.read(reference));
        storage.delete(reference);
        assertFalse(Files.exists(Path.of(reference)));
    }

    @Test
    void rejectsUnsupportedStorageProvider() {
        assertThrows(IllegalArgumentException.class,
                () -> new FileStorageService("unknown", tempDir.toString(), "", "", ""));
    }
}
