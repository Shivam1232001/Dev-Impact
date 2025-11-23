package com.example.dev_impact.controller;

import com.example.dev_impact.dto.AuthenticationResponseDTO;
import com.example.dev_impact.model.User;
import com.example.dev_impact.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1.0.0/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(@RequestBody User request) {
        logger.info("Attempting to register new user with username: {}", request.getUsername());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody User request) {
        logger.info("Attempting login for user: {}", request.getUsername());
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/github/oauth")
    public String githubOAuth(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        logger.debug("Request received for GitHub OAuth URL for user ID: {}", user.getId());
        return authService.getGithubOAuthUrl(user);
    }

    @GetMapping("/github/status")
    public boolean githubConnectionStatus(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        logger.debug("Checking GitHub connection status for user ID: {}", user.getId());
        return authService.getGithubConnectionStatus(user);
    }
}