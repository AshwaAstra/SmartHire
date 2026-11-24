package com.recruitment.repository;
import com.recruitment.entity.Candidate; import com.recruitment.entity.ResumeImprovement;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ResumeImprovementRepository extends JpaRepository<ResumeImprovement,Long>{List<ResumeImprovement> findByCandidateOrderByCreatedAtDesc(Candidate candidate);}
