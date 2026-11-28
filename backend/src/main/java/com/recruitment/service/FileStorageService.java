package com.recruitment.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final String CLOUDINARY_PREFIX = "cloudinary|";
    private static final Set<String> RESOURCE_TYPES = Set.of("image", "video", "raw");

    private final String provider;
    private final Path root;
    private final Cloudinary cloudinary;
    private static final long PHOTO_CACHE_MILLIS = 10 * 60 * 1000L;
    private final Map<String, CachedPhoto> photoCache = new ConcurrentHashMap<>();

    public FileStorageService(
            @Value("${app.file-storage.provider:local}") String provider,
            @Value("${app.file-storage.root:uploads}") String root,
            @Value("${app.file-storage.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.file-storage.cloudinary.api-key:}") String apiKey,
            @Value("${app.file-storage.cloudinary.api-secret:}") String apiSecret) {
        this.provider = provider.toLowerCase(Locale.ROOT);
        this.root = Path.of(root).toAbsolutePath().normalize();
        if ("cloudinary".equals(this.provider)) {
            if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
                throw new IllegalStateException("Cloudinary storage requires CLOUDINARY_CLOUD_NAME, "
                        + "CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET");
            }
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true));
        } else if ("local".equals(this.provider)) {
            this.cloudinary = null;
        } else {
            throw new IllegalArgumentException("FILE_STORAGE_PROVIDER must be 'local' or 'cloudinary'");
        }
    }

    public String store(MultipartFile file, String folder, String resourceType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file");
        }
        if (!RESOURCE_TYPES.contains(resourceType)) {
            throw new IllegalArgumentException("Unsupported Cloudinary resource type");
        }
        String extension = extension(file.getOriginalFilename());
        if ("cloudinary".equals(provider)) {
            String publicId = "smarthire/" + folder + "/" + UUID.randomUUID();
            if ("raw".equals(resourceType)) {
                publicId += extension;
            }
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "type", "authenticated",
                    "public_id", publicId,
                    "overwrite", false));
            String savedPublicId = result.get("public_id").toString();
            String format = result.get("format") == null ? "" : result.get("format").toString();
            log.info("Cloudinary stored {} asset {}", resourceType, savedPublicId);
            return CLOUDINARY_PREFIX + resourceType + "|" + format + "|" + savedPublicId;
        }

        Path directory = root.resolve(folder).normalize();
        if (!directory.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage folder");
        }
        Files.createDirectories(directory);
        Path target = directory.resolve(UUID.randomUUID() + extension);
        try (var input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toString();
    }

    public byte[] read(String reference) throws IOException {
        if (!isCloudinaryReference(reference)) {
            return Files.readAllBytes(Path.of(reference));
        }
        AssetReference asset = parse(reference);
        var urlBuilder = cloudinary.url()
                .resourceType(asset.resourceType())
                .type("authenticated")
                .signed(true);
        if (!"raw".equals(asset.resourceType()) && !asset.format().isBlank()) {
            urlBuilder.format(asset.format());
        }
        try {
            URLConnection connection = URI.create(urlBuilder.generate(asset.publicId())).toURL().openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            try (var input = connection.getInputStream(); var output = new ByteArrayOutputStream()) {
                input.transferTo(output);
                return output.toByteArray();
            }
        } catch (IOException exception) {
            log.warn("Cloudinary could not read {} asset {} ({})", asset.resourceType(), asset.publicId(), exception.getClass().getSimpleName());
            throw exception;
        }
    }

    /** Caches already-authorized profile image bytes and avoids repeat Cloudinary downloads. */
    public byte[] readPhoto(String reference) throws IOException {
        CachedPhoto cached = photoCache.get(reference);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) return cached.bytes();
        byte[] bytes = read(reference);
        if (bytes.length <= 5 * 1024 * 1024) {
            photoCache.put(reference, new CachedPhoto(bytes, Instant.now().plusMillis(PHOTO_CACHE_MILLIS)));
        }
        return bytes;
    }

    public void delete(String reference) throws IOException {
        photoCache.remove(reference);
        if (isCloudinaryReference(reference)) {
            AssetReference asset = parse(reference);
            Map<?, ?> result = cloudinary.uploader().destroy(asset.publicId(), ObjectUtils.asMap(
                    "resource_type", asset.resourceType(),
                    "type", "authenticated"));
            Object status = result.get("result");
            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new IOException("Cloudinary could not delete the stored file");
            }
            return;
        }

        Path file = Path.of(reference).toAbsolutePath().normalize();
        if (file.startsWith(root)) {
            Files.deleteIfExists(file);
        }
    }

    public String contentType(String reference) throws IOException {
        if (isCloudinaryReference(reference)) {
            String format = parse(reference).format().toLowerCase(Locale.ROOT);
            return switch (format) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                default -> "application/octet-stream";
            };
        }
        String contentType = Files.probeContentType(Path.of(reference));
        return contentType == null ? "application/octet-stream" : contentType;
    }

    private boolean isCloudinaryReference(String reference) {
        return reference != null && reference.startsWith(CLOUDINARY_PREFIX);
    }

    private AssetReference parse(String reference) {
        String[] parts = reference.substring(CLOUDINARY_PREFIX.length()).split("\\|", 3);
        if (parts.length != 3 || !RESOURCE_TYPES.contains(parts[0]) || parts[2].isBlank()) {
            throw new IllegalArgumentException("Invalid Cloudinary file reference");
        }
        return new AssetReference(parts[0], parts[1], parts[2]);
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = filename.substring(dot).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
    }

    private record AssetReference(String resourceType, String format, String publicId) {}
    private record CachedPhoto(byte[] bytes, Instant expiresAt) {}
}
