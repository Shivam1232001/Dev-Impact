package com.example.dev_impact.repository;

import com.example.dev_impact.model.CodeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeAnalysisRepository extends JpaRepository<com.example.dev_impact.model.CodeAnalysis, Long> {

    List<CodeAnalysis> findByUserId(Long userId);
}
