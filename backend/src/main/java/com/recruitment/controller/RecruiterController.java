package com.recruitment.controller;

import com.recruitment.dto.ProfileDtos.RecruiterProfile;
import com.recruitment.entity.Recruiter;
import com.recruitment.entity.User;
import com.recruitment.repository.RecruiterRepository;
import com.recruitment.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import com.recruitment.service.FileStorageService;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@RestController @RequestMapping("/api/recruiters")
@PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
public class RecruiterController {
    private final UserRepository users;
    private final RecruiterRepository recruiters;
    private final FileStorageService fileStorage;
    public RecruiterController(UserRepository users, RecruiterRepository recruiters, FileStorageService fileStorage){this.users=users;this.recruiters=recruiters;this.fileStorage=fileStorage;}

    private Recruiter current(Authentication authentication){
        User user=users.findByEmail(authentication.getName()).orElseThrow();
        return recruiters.findByUser(user).orElseGet(() -> { Recruiter r=new Recruiter(); r.setUser(user); return recruiters.save(r); });
    }
    @GetMapping("/profile")
    public RecruiterProfile profile(Authentication authentication){
        User u=users.findByEmail(authentication.getName()).orElseThrow(); Recruiter r=current(authentication);
        return new RecruiterProfile(u.getName(),u.getEmail(),r.getPhone(),r.getCompanyName(),r.getCompanyDescription(),r.getCompanyWebsite(),r.getCompanyLocation(),r.getPhotoPath());
    }
    @PutMapping("/profile")
    public RecruiterProfile update(@Valid @RequestBody RecruiterProfile req, Authentication authentication){
        User u=users.findByEmail(authentication.getName()).orElseThrow(); Recruiter r=current(authentication);
        u.setName(req.name()); users.save(u);
        r.setPhone(req.phone()); r.setCompanyName(req.companyName().trim()); r.setCompanyDescription(req.companyDescription());
        r.setCompanyWebsite(req.companyWebsite()); r.setCompanyLocation(req.companyLocation()); recruiters.save(r);
        return profile(authentication);
    }

    @PostMapping(value="/photo", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecruiterProfile> uploadPhoto(@RequestPart("file") MultipartFile file, Authentication authentication) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Choose a profile photo first");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Profile photo exceeds 5 MB");
        String type=file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(type) && !MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(type))
            throw new IllegalArgumentException("Only JPEG and PNG photos are accepted");
        try (InputStream input=file.getInputStream()) {
            if (ImageIO.read(input)==null) throw new IllegalArgumentException("The selected file is not a valid image");
        }
        Recruiter recruiter=current(authentication);
        String previous=recruiter.getPhotoPath();
        String path=fileStorage.store(file,"recruiter-photos","image");
        recruiter.setPhotoPath(path); recruiters.save(recruiter);
        if (previous!=null && !previous.equals(path)) fileStorage.delete(previous);
        return ResponseEntity.ok(profile(authentication));
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> photo(Authentication authentication) {
        Recruiter recruiter=current(authentication);
        if (recruiter.getPhotoPath()==null) return ResponseEntity.notFound().build();
        try {
            return ResponseEntity.ok().cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePrivate())
                    .contentType(MediaType.parseMediaType(fileStorage.contentType(recruiter.getPhotoPath())))
                    .body(fileStorage.readPhoto(recruiter.getPhotoPath()));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary could not load this photo. Upload it again and retry.");
        }
    }
}
