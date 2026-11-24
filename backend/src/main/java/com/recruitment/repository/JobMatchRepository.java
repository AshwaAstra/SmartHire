package com.recruitment.repository;
import com.recruitment.entity.Candidate; import com.recruitment.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface JobMatchRepository extends JpaRepository<JobMatch,Long>{List<JobMatch> findByCandidate(Candidate c);}
