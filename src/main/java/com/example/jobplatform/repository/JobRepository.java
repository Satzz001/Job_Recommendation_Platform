package com.example.jobplatform.repository;

import com.example.jobplatform.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    interface JobSimilarityResult {
        Long getJobId();
        Double getSimilarity();
    }

    @Query(value = """
        SELECT j.id AS jobId, 1 - (up.summary_embedding <=> j.description_embedding) AS similarity
        FROM jobs j
        JOIN user_profile up ON up.user_id = :userId
        WHERE j.id IN (:jobIds)
          AND up.summary_embedding IS NOT NULL
          AND j.description_embedding IS NOT NULL
        """, nativeQuery = true)
    List<JobSimilarityResult> getSemanticSimilarities(@Param("userId") Long userId, @Param("jobIds") List<Long> jobIds);

    interface JobSearchResult {
        Long getId();
        Double getSimilarity();
    }

    @Query(value = """
        SELECT j.id AS id, 1 - (j.description_embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM jobs j
        WHERE j.description_embedding IS NOT NULL
        ORDER BY j.description_embedding <=> CAST(:embedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<JobSearchResult> findTopSimilarJobs(@Param("embedding") String embedding, @Param("topK") int topK);
}