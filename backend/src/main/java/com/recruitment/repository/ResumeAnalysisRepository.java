package com.recruitment.repository;
import com.recruitment.entity.Candidate; import com.recruitment.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis,Long>{List<ResumeAnalysis> findByCandidate(Candidate c); java.util.Optional<ResumeAnalysis> findFirstByCandidateOrderByAnalyzedAtDesc(Candidate c);}
