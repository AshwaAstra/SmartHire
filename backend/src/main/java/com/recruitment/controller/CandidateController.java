package com.recruitment.controller;

import com.recruitment.dto.ProfileDtos.CandidateProfile;
import com.recruitment.entity.Candidate;
import com.recruitment.entity.User;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@RestController @RequestMapping("/api/candidates")
@PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
public class CandidateController {
    private final UserRepository users;
    private final CandidateRepository candidates;

    public CandidateController(UserRepository users, CandidateRepository candidates) {
        this.users=users; this.candidates=candidates;
    }

    private Candidate current(Authentication authentication) {
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        return candidates.findByUser(user).orElseGet(() -> candidates.save(candidateFor(user)));
    }
    private Candidate candidateFor(User user) { Candidate c=new Candidate(); c.setUser(user); return c; }

    @GetMapping("/profile")
    public CandidateProfile profile(Authentication authentication) {
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        Candidate c=current(authentication);
        return new CandidateProfile(user.getName(), user.getEmail(), c.getPhone(), c.getLocation(),
                c.getEducation(), c.getGraduationYear(), c.getSkills(), c.getExperience(),
                c.getProfessionalSummary(), c.getResumePath(), c.getPhotoPath());
    }

    @PutMapping("/profile")
    public CandidateProfile updateProfile(@Valid @RequestBody CandidateProfile request, Authentication authentication) {
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        Candidate c=current(authentication);
        user.setName(request.name()); users.save(user);
        c.setPhone(request.phone()); c.setLocation(request.location()); c.setEducation(request.education());
        c.setGraduationYear(request.graduationYear()); c.setSkills(request.skills());
        c.setExperience(request.experience()); c.setProfessionalSummary(request.professionalSummary());
        c=candidates.save(c);
        return profile(authentication);
    }

    @PostMapping(value="/resume", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfile> uploadResume(@RequestPart("file") MultipartFile file,
                                                         Authentication authentication) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Resume file is required");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new IllegalArgumentException("Only PDF resumes are accepted");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Resume exceeds 10 MB");
        Path dir=Paths.get("uploads","resumes"); Files.createDirectories(dir);
        String safeName=System.currentTimeMillis()+"-"+Path.of(file.getOriginalFilename()).getFileName();
        Path target=dir.resolve(safeName); Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        Candidate c=current(authentication); c.setResumePath(target.toString().replace('\\','/')); candidates.save(c);
        return ResponseEntity.ok(profile(authentication));
    }

    @GetMapping("/resume")
    public ResponseEntity<byte[]> downloadResume(Authentication authentication) throws Exception {
        Candidate c=current(authentication);
        if (c.getResumePath()==null) return ResponseEntity.notFound().build();
        byte[] bytes=Files.readAllBytes(Paths.get(c.getResumePath()));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);
    }

    @PostMapping(value="/photo", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfile> uploadPhoto(@RequestPart("file") MultipartFile file,
                                                         Authentication authentication) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Choose a profile photo first");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Profile photo exceeds 5 MB");

        String contentType=file.getContentType();
        String extension;
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)) extension=".jpg";
        else if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)) extension=".png";
        else throw new IllegalArgumentException("Only JPEG and PNG photos are accepted");
        try (InputStream input=file.getInputStream()) {
            if (ImageIO.read(input) == null) throw new IllegalArgumentException("The selected file is not a valid image");
        }

        Path dir=Paths.get("uploads","photos").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target=dir.resolve(UUID.randomUUID()+extension);
        try (InputStream input=file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Candidate candidate=current(authentication);
        Path previous=candidate.getPhotoPath()==null ? null : Paths.get(candidate.getPhotoPath()).toAbsolutePath().normalize();
        candidate.setPhotoPath(target.toString());
        candidates.save(candidate);
        if (previous != null && previous.startsWith(dir) && !previous.equals(target)) Files.deleteIfExists(previous);
        return ResponseEntity.ok(profile(authentication));
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> downloadPhoto(Authentication authentication) throws IOException {
        Candidate candidate=current(authentication);
        if (candidate.getPhotoPath()==null) return ResponseEntity.notFound().build();
        Path dir=Paths.get("uploads","photos").toAbsolutePath().normalize();
        Path photo=Paths.get(candidate.getPhotoPath()).toAbsolutePath().normalize();
        if (!photo.startsWith(dir) || !Files.isRegularFile(photo)) return ResponseEntity.notFound().build();
        MediaType contentType=photo.toString().toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(contentType).body(Files.readAllBytes(photo));
    }
}
