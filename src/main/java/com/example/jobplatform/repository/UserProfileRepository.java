package com.example.jobplatform.repository;

import com.example.jobplatform.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    @Modifying
    @Query(value = "UPDATE user_profile SET summary_embedding = CAST(:embedding AS vector) WHERE user_id = :userId", nativeQuery = true)
    void updateSummaryEmbedding(@Param("userId") Long userId, @Param("embedding") String embedding);

    @Query(value = "SELECT summary_embedding::text FROM user_profile WHERE user_id = :userId", nativeQuery = true)
    Optional<String> findSummaryEmbeddingByUserId(@Param("userId") Long userId);
}