package com.recruitment.controller;

import com.recruitment.dto.ProfileDtos.CandidateProfile;
import com.recruitment.entity.Candidate;
import com.recruitment.entity.User;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@RestController @RequestMapping("/api/candidates")
@PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
public class CandidateController {
    private final UserRepository users;
    private final CandidateRepository candidates;
    private final FileStorageService fileStorage;

    public CandidateController(UserRepository users, CandidateRepository candidates, FileStorageService fileStorage) {
        this.users=users; this.candidates=candidates; this.fileStorage=fileStorage;
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
                c.getProfessionalSummary(), c.getResumePath(), c.getPhotoPath(), c.getResumeFileName(), c.getLatitude(), c.getLongitude());
    }

    @PutMapping("/profile")
    public CandidateProfile updateProfile(@Valid @RequestBody CandidateProfile request, Authentication authentication) {
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        Candidate c=current(authentication);
        user.setName(request.name()); users.save(user);
        c.setPhone(request.phone()); c.setLocation(request.location()); c.setEducation(request.education());
        c.setGraduationYear(request.graduationYear()); c.setSkills(request.skills());
        c.setExperience(request.experience()); c.setProfessionalSummary(request.professionalSummary());
        c.setLatitude(request.latitude()); c.setLongitude(request.longitude());
        c=candidates.save(c);
        return profile(authentication);
    }

    @GetMapping("/location")
    public java.util.Map<String,Object> location(Authentication authentication) {
        Candidate candidate=current(authentication);
        if(candidate.getLatitude()==null||candidate.getLongitude()==null)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Save your current location in your profile first");
        return java.util.Map.of("latitude",candidate.getLatitude(),"longitude",candidate.getLongitude(),"location",candidate.getLocation()==null?"":candidate.getLocation());
    }

    @PostMapping(value="/resume", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfile> uploadResume(@RequestPart("file") MultipartFile file,
                                                         Authentication authentication) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Resume file is required");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new IllegalArgumentException("Only PDF resumes are accepted");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Resume exceeds 10 MB");
        String path=fileStorage.store(file, "resumes", "raw");
        Candidate c=current(authentication);
        String previous=c.getResumePath();
        c.setResumePath(path);
        c.setResumeFileName(org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename()==null?"resume.pdf":file.getOriginalFilename()));
        candidates.save(c);
        if (previous!=null && !previous.equals(path)) fileStorage.delete(previous);
        return ResponseEntity.ok(profile(authentication));
    }

    @GetMapping("/resume")
    public ResponseEntity<byte[]> downloadResume(Authentication authentication) throws Exception {
        Candidate c=current(authentication);
        if (c.getResumePath()==null) return ResponseEntity.notFound().build();
        byte[] bytes=fileStorage.read(c.getResumePath());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);
    }

    @PostMapping(value="/photo", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfile> uploadPhoto(@RequestPart("file") MultipartFile file,
                                                         Authentication authentication) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Choose a profile photo first");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Profile photo exceeds 5 MB");

        String contentType=file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)
                && !MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType))
            throw new IllegalArgumentException("Only JPEG and PNG photos are accepted");
        try (InputStream input=file.getInputStream()) {
            if (ImageIO.read(input) == null) throw new IllegalArgumentException("The selected file is not a valid image");
        }

        Candidate candidate=current(authentication);
        String previous=candidate.getPhotoPath();
        String path=fileStorage.store(file, "photos", "image");
        candidate.setPhotoPath(path);
        candidates.save(candidate);
        if (previous != null && !previous.equals(path)) fileStorage.delete(previous);
        return ResponseEntity.ok(profile(authentication));
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> downloadPhoto(Authentication authentication) {
        Candidate candidate=current(authentication);
        if (candidate.getPhotoPath()==null) return ResponseEntity.notFound().build();
        try {
            String contentType=fileStorage.contentType(candidate.getPhotoPath());
            return ResponseEntity.ok().cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePrivate())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileStorage.readPhoto(candidate.getPhotoPath()));
        } catch (FileNotFoundException exception) {
            candidate.setPhotoPath(null);
            candidates.save(candidate);
            return ResponseEntity.notFound().build();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary could not load this photo. Upload it again and retry.");
        }
    }
}
