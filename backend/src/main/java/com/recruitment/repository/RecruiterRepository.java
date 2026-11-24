package com.recruitment.repository;
import com.recruitment.entity.Recruiter;
import com.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> { Optional<Recruiter> findByUser(User user); }
