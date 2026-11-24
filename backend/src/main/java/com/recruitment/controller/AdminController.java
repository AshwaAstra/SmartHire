package com.recruitment.controller;

import com.recruitment.entity.User;
import com.recruitment.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository users;
    public AdminController(UserRepository users){this.users=users;}
    @GetMapping("/users") public List<User> users(){return users.findAll();}
    @PatchMapping("/users/{id}/status")
    public User status(@PathVariable Long id,@RequestParam boolean enabled){User u=users.findById(id).orElseThrow();u.setEnabled(enabled);return users.save(u);}
}
