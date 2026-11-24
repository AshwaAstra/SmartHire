package com.recruitment.repository;
import com.recruitment.entity.Job;
import com.recruitment.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByActiveTrue();
    List<Job> findByRecruiter(Recruiter recruiter);
}
