package com.recruitment.repository;
import com.recruitment.entity.Candidate; import com.recruitment.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion,Long>{List<InterviewQuestion> findByCandidate(Candidate candidate);}
