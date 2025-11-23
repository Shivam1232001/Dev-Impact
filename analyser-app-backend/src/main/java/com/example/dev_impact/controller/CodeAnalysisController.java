package com.example.dev_impact.controller;

import com.example.dev_impact.model.CodeAnalysis;
import com.example.dev_impact.model.User;
import com.example.dev_impact.service.CodeAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1.0.0/analyze")
public class CodeAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(CodeAnalysisController.class);

    @Autowired
    private CodeAnalysisService codeAnalysisService;

    @PostMapping("/repo")
    public HashMap<String, String> analyzeCode(String repoUrl, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        logger.info("User {} starting analysis for repo: {}", user.getUsername(), repoUrl);
        return codeAnalysisService.startAnalyzingCode(repoUrl, user);
    }

    @PostMapping("/callback/{id}")
    public boolean receiveAnalysisResult(@PathVariable Long id, @RequestBody JsonNode report) {
        logger.info("Received analysis callback for ID: {}", id);
        return codeAnalysisService.handleAnalysisCallback(id, report);
    }

    @GetMapping("/allProjects")
    public List<CodeAnalysis> getAllAnalyses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        logger.debug("Fetching all analyses for user: {}", user.getUsername());
        return codeAnalysisService.getAllAnalysesForUser(user);
    }

    @GetMapping("/project/{id}")
    public CodeAnalysis getAnalysisById(@PathVariable Long id, Authentication authentication) throws Exception {
        User user = (User) authentication.getPrincipal();
        logger.debug("Fetching analysis ID: {} for user: {}", id, user.getUsername());
        return codeAnalysisService.getCodeAnalysisById(id);
    }
}