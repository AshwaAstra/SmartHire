package com.recruitment.repository;
import com.recruitment.entity.InterviewResponse; import com.recruitment.entity.MockInterview;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface InterviewResponseRepository extends JpaRepository<InterviewResponse,Long>{List<InterviewResponse> findByMockInterview(MockInterview interview);}
