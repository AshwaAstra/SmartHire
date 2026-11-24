package com.recruitment.repository;
import com.recruitment.entity.Application;
import com.recruitment.entity.Candidate;
import com.recruitment.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByCandidateAndJob(Candidate candidate, Job job);
    List<Application> findByCandidate(Candidate candidate);
    List<Application> findByJob(Job job);
}
