package com.recruitment.repository;
import com.recruitment.entity.Job;
import com.recruitment.entity.Recruiter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "recruiter")
    List<Job> findByRecruiter(Recruiter recruiter);
}
