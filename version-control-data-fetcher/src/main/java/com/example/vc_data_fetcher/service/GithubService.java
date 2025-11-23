package com.example.vc_data_fetcher.service;

import com.example.vc_data_fetcher.model.VCToken;
import com.example.vc_data_fetcher.repository.VCTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    private final VCTokenRepository vcTokenRepository;

    public GithubService(VCTokenRepository vcTokenRepository) {
        this.vcTokenRepository = vcTokenRepository;
    }

    public String exchangeCodeForAccessToken(String code) {
        logger.debug("Starting code exchange without explicit user ID.");
        return exchangeCodeForAccessToken(code, null);
    }

    public String exchangeCodeForAccessToken(String code, Long userId) {
        logger.info("Attempting to exchange OAuth code for access token. User ID: {}", userId);
        String url = "https://github.com/login/oauth/access_token";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("code", code);
        body.put("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        logger.debug("Received response from GitHub access token endpoint. Status: {}", response.getStatusCode());

        if (response.getBody() != null && response.getBody().get("access_token") != null) {
            String accessToken = response.getBody().get("access_token").toString();

            // Store the access token in database
            saveTokenToDatabase(userId, accessToken, response.getBody());
            logger.info("Access token successfully retrieved and stored for user ID: {}", userId);

            return accessToken;
        }

        logger.error("Failed to retrieve access token from GitHub for user ID: {}", userId);
        throw new RuntimeException("Failed to retrieve access token from GitHub");
    }

    private void saveTokenToDatabase(Long userId, String accessToken, Map<String, Object> githubResponse) {
        try {
            logger.debug("Saving/updating token in database for user ID: {}", userId);
            VCToken vcToken;

            // Check if user already has a token, update if exists
            if (userId != null) {
                Optional<VCToken> existingToken = vcTokenRepository.findByUserId(userId);
                if (existingToken.isPresent()) {
                    vcToken = existingToken.get();
                    logger.debug("Existing token found for user ID: {}", userId);
                } else {
                    vcToken = new VCToken();
                    vcToken.setUser_id(userId);
                }
            } else {
                vcToken = new VCToken();
            }

            vcToken.setAccess_token(accessToken);

            // Store additional information from GitHub response as JSON string
            if (githubResponse.size() > 1) {
                StringBuilder miscInfo = new StringBuilder();
                githubResponse.forEach((key, value) -> {
                    if (!"access_token".equals(key)) {
                        miscInfo.append(key).append(":").append(value).append(";");
                    }
                });
                vcToken.setMisc_info(miscInfo.toString());
            }

            vcTokenRepository.save(vcToken);

        } catch (Exception e) {
            // Log the error but don't fail the OAuth flow
            System.err.println("Failed to save token to database: " + e.getMessage());
            logger.error("Failed to save token to database for user ID {}: {}", userId, e.getMessage());
        }
    }
    public Optional<VCToken> getTokenByUserId(Long userId) {
        logger.debug("Fetching token for user ID: {}", userId);
        return vcTokenRepository.findByUserId(userId);
    }

    public boolean hasValidToken(Long userId) {
        logger.debug("Checking if valid token exists for user ID: {}", userId);
        return userId != null && vcTokenRepository.existsByUserId(userId);
    }
}