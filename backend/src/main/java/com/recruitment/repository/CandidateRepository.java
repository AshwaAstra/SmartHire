package com.recruitment.repository;
import com.recruitment.entity.Candidate;
import com.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CandidateRepository extends JpaRepository<Candidate, Long> { Optional<Candidate> findByUser(User user); }
