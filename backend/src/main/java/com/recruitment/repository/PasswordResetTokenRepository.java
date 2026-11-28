package com.recruitment.repository;

import com.recruitment.entity.PasswordResetToken;
import com.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}
