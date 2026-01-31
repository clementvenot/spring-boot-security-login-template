package com.template.repository;

import com.template.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :now")
    int deleteAllExpired(@Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("delete from PasswordResetToken t where t.usedAt is not null and t.usedAt < :threshold")
    int deleteAllUsedBefore(@Param("threshold") Instant threshold);
}