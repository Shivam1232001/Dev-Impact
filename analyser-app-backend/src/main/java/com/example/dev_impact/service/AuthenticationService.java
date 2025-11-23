package com.example.dev_impact.service;

import com.example.dev_impact.dto.AuthenticationResponseDTO;
import com.example.dev_impact.model.Token;
import com.example.dev_impact.model.User;
import com.example.dev_impact.repository.TokenRepository;
import com.example.dev_impact.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;

    @Value("${version_control_service.url}")
    private String vcsServiceUrl;

    public AuthenticationService(UserRepository repository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 TokenRepository tokenRepository,
                                 AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDTO register(User request) {
        if(repository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Registration failed: User already exists with username: {}", request.getUsername());
            return new AuthenticationResponseDTO(null, "User already exist", null);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = repository.save(user);
        String jwt = jwtService.generateToken(user);
        saveUserToken(jwt, user);

        logger.info("New user registered successfully: {}", user.getUsername());
        return new AuthenticationResponseDTO(jwt, "User registration was successful", user.getFirstName() + " " + user.getLastName());

    }

    public AuthenticationResponseDTO authenticate(User request) {
        logger.info("Authenticating user: {}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String jwt = jwtService.generateToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(jwt, user);

        logger.info("User {} authenticated and new token issued.", user.getUsername());
        return new AuthenticationResponseDTO(jwt, "User login was successful", user.getFirstName() + " " + user.getLastName());

    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            logger.debug("No active tokens to revoke for user ID: {}", user.getId());
            return;
        }

        logger.info("Revoking {} tokens for user ID: {}", validTokens.size(), user.getId());
        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String jwt, User user) {
        Token token = new Token();
        token.setToken(jwt);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
        logger.debug("Token saved for user ID: {}", user.getId());
    }

    public String getGithubOAuthUrl(User user) {
        logger.info("Requesting GitHub OAuth URL for user ID: {}", user.getId());
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI( vcsServiceUrl+ "/api/v1.0.0/auth/github-login?user_id=" + user.getId().toString()))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Received GitHub OAuth URL response status: {}", response.statusCode());
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Failed to get GitHub OAuth URL for user ID {}: {}", user.getId(), e.getMessage());
            return "";
        }
    }

    public boolean getGithubConnectionStatus(User user) {
        logger.info("Checking GitHub connection status for user ID: {}", user.getId());
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI( vcsServiceUrl+ "/api/v1.0.0/auth/check-token?user_id=" + user.getId().toString()))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("GitHub status check response code: {}", response.statusCode());
            return response.statusCode() == 200;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Failed to check GitHub connection status for user ID {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }
}