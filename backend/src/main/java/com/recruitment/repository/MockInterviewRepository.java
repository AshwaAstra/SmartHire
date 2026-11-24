package com.recruitment.repository;
import com.recruitment.entity.Candidate; import com.recruitment.entity.MockInterview;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface MockInterviewRepository extends JpaRepository<MockInterview,Long>{List<MockInterview> findByCandidate(Candidate candidate);}
