package com.example.vc_data_fetcher.controller;

import com.example.vc_data_fetcher.api.GithubAPI;
import com.example.vc_data_fetcher.model.VCToken;
import com.example.vc_data_fetcher.service.GithubService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class GithubController implements GithubAPI {

	private static final Logger logger = LoggerFactory.getLogger(GithubController.class);

	private final GithubService githubService;

	@Value("${github.client.id}")
	private String clientId;

	@Value("${github.redirect.uri}")
	private String redirectUri;

	@Value("${frontend.url:http://localhost:3000}")
	private String frontendUrl;

	public GithubController(GithubService githubService) {
		this.githubService = githubService;
	}

	@Override
	public String login(HttpServletResponse response, Long userId) throws IOException {
		logger.info("Generating GitHub OAuth URL for user ID: {}", userId);
		String githubAuthUrl = "https://github.com/login/oauth/authorize"
				+ "?client_id=" + clientId
				+ "&redirect_uri=" + redirectUri
				+ "&scope=repo,user";

		if (userId != null) {
			githubAuthUrl += "&state=" + userId;
		}

		return githubAuthUrl;
	}

	@Override
	public void callback(String code, String state, HttpServletResponse response) throws IOException {
		logger.info("Received GitHub callback with code and state: {}", state);
		try {
			Long userId = null;
			if (state != null && !state.isEmpty()) {
				try {
					userId = Long.parseLong(state);
					logger.debug("Parsed user ID from state: {}", userId);
				} catch (NumberFormatException e) {
					System.err.println("Invalid user_id in state parameter: " + state);
					logger.error("Invalid user_id in state parameter: {}", state);
				}
			}

			String accessToken = githubService.exchangeCodeForAccessToken(code, userId);
			logger.info("Successfully exchanged code for access token for user ID: {}", userId);

			String redirectUrl = frontendUrl + "?auth=success&token_stored=true";
			if (userId != null) {
				redirectUrl += "&user_id=" + userId;
			}
			response.sendRedirect(redirectUrl);

		} catch (Exception e) {
			logger.error("Error during GitHub callback processing: {}", e.getMessage());
			response.sendRedirect(frontendUrl + "?auth=error&message=" + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> exchangeToken(String code, Long userId) {
		logger.info("Attempting direct token exchange for user ID: {}", userId);
		try {
			String accessToken = githubService.exchangeCodeForAccessToken(code, userId);
			logger.info("Token successfully exchanged and stored for user ID: {}", userId);
			return ResponseEntity.ok().body(Map.of(
					"access_token", accessToken,
					"message", "Token stored successfully",
					"user_id", userId != null ? userId : "null"
			));
		} catch (Exception e) {
			logger.error("Failed to exchange code for token for user ID {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to exchange code for token: " + e.getMessage()));
		}
	}

	// TODO: Add functionality to check token expiration
	@Override
	public ResponseEntity checkToken(Long userId) {
		logger.debug("Checking token presence for user ID: {}", userId);
		try {
			Optional<VCToken> token = githubService.getTokenByUserId(userId);
			if (token.isPresent()) {
				logger.info("Token found for user ID: {}", userId);
				return ResponseEntity.status(HttpStatus.OK).build();
			} else {
				logger.warn("No token found for user ID: {}", userId);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

			}
		} catch (Exception e) {
			logger.error("Failed to check token status for user ID {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to check token: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> getToken(Long userId) {
		logger.debug("Retrieving token details for user ID: {}", userId);
		try {
			Optional<VCToken> token = githubService.getTokenByUserId(userId);
			if (token.isPresent()) {
				logger.info("Token details retrieved for user ID: {}", userId);
				return ResponseEntity.ok().body(Map.of(
						"user_id", userId,
						"access_token", token.get().getAccess_token(),
						"misc_info", token.get().getMisc_info() != null ? token.get().getMisc_info() : ""
				));
			} else {
				logger.warn("Token not found during retrieval attempt for user ID: {}", userId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("error", "No token found for user_id: " + userId));
			}
		} catch (Exception e) {
			logger.error("Failed to retrieve token for user ID {}: {}", userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Failed to retrieve token: " + e.getMessage()));
		}
	}
}