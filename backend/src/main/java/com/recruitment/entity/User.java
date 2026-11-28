package com.recruitment.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String email;
    @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    @Column(nullable=false) private boolean enabled = true;
    @Column(name="token_version", nullable=false, columnDefinition="integer default 0") private int tokenVersion = 0;
    @Column(nullable=false, updatable=false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
    public Role getRole(){return role;} public void setRole(Role role){this.role=role;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean enabled){this.enabled=enabled;}
    public int getTokenVersion(){return tokenVersion;} public void setTokenVersion(int tokenVersion){this.tokenVersion=tokenVersion;}
    public Instant getCreatedAt(){return createdAt;}
}
