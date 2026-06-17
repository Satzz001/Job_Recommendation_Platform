package com.example.jobplatform.repository;

import com.example.jobplatform.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Used by ResumeParsingService to find or create a skill by name
    Optional<Skill> findByName(String name);

    Optional<Skill> findByNameIgnoreCase(String name);

    @Query(value = """
        SELECT s2.id
        FROM skills s1
        JOIN skills s2 ON s1.id != s2.id
        WHERE s1.id = :skillId
          AND s2.id IN (:candidateIds)
          AND 1 - (s1.embedding <=> s2.embedding) >= :threshold
        """, nativeQuery = true)
    List<Long> findSimilarSkillIds(
        @Param("skillId") Long skillId,
        @Param("candidateIds") List<Long> candidateIds,
        @Param("threshold") double threshold
    );

    @Query(value = """
        SELECT DISTINCT s2.id
        FROM skills s1
        JOIN skills s2 ON s1.id != s2.id
        WHERE s1.id IN (:skillIds)
          AND s2.id NOT IN (:skillIds)
          AND 1 - (s1.embedding <=> s2.embedding) >= :threshold
        """, nativeQuery = true)
    List<Long> findAllSimilarSkillIds(
        @Param("skillIds") List<Long> skillIds,
        @Param("threshold") double threshold
    );
}