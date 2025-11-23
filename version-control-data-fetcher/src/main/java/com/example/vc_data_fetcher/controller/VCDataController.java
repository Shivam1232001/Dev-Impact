package com.example.vc_data_fetcher.controller;

import com.example.vc_data_fetcher.dto.CheckAccessRequest;
import com.example.vc_data_fetcher.dto.ContributorWithCommits;
import com.example.vc_data_fetcher.service.VCDataService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RestController
@RequestMapping("/api/v1.0.0")
public class VCDataController {

	private static final Logger logger = LoggerFactory.getLogger(VCDataController.class);

	private final VCDataService vcDataService;

	public VCDataController(VCDataService vcDataService) {
		this.vcDataService = vcDataService;
	}

	/**
	 * Check if user has access to a repository
	 */
	@QueryMapping
	public ResponseEntity checkAccess(@Argument String repoUrl, @Argument Long userId) {
		logger.info("GraphQL: Checking access for repo: {} by user ID: {}", repoUrl, userId);
		try {
			return vcDataService.checkRepoAccess(repoUrl, userId);
		} catch (IllegalArgumentException e) {
			logger.warn("GraphQL: Invalid repository URL: {}", e.getMessage());
			throw new GraphQLException("Invalid repository URL: " + e.getMessage());
		} catch (RuntimeException e) {
			logger.error("GraphQL: Runtime error checking access: {}", e.getMessage());
			throw new GraphQLException("Error checking repository access: " + e.getMessage());
		} catch (Exception e) {
			logger.error("GraphQL: Unexpected error checking access: {}", e.getMessage());
			throw new GraphQLException("Unexpected error: " + e.getMessage());
		}
	}

	@PostMapping("/repo-validate")
	public ResponseEntity checkAccess(@RequestBody CheckAccessRequest request) {
		logger.info("REST: Validating repo access for repo: {} by user ID: {}", request.getRepoUrl(), request.getUserId());
		try {
			return vcDataService.checkRepoAccess(request.getRepoUrl(), request.getUserId());
		} catch (IllegalArgumentException e) {
			logger.warn("REST: Invalid repository URL: {}", e.getMessage());
			throw new GraphQLException("Invalid repository URL: " + e.getMessage());
		} catch (RuntimeException e) {
			logger.error("REST: Runtime error checking access: {}", e.getMessage());
			throw new GraphQLException("Error checking repository access: " + e.getMessage());
		} catch (Exception e) {
			logger.error("REST: Unexpected error checking access: {}", e.getMessage());
			throw new GraphQLException("Unexpected error: " + e.getMessage());
		}
	}

	/**
	 * Get contributors along with their commits in one aggregated query
	 */
	@QueryMapping
	public List<ContributorWithCommits> repoData(@Argument String repoUrl, @Argument Long userId) {
		logger.info("GraphQL: Fetching repo data for repo: {} by user ID: {}", repoUrl, userId);
		try {
			return vcDataService.getContributorsWithCommits(repoUrl, userId);
		} catch (IllegalArgumentException e) {
			logger.warn("GraphQL: Invalid repository URL for repo data: {}", e.getMessage());
			throw new GraphQLException("Invalid repository URL: " + e.getMessage());
		} catch (RuntimeException e) {
			logger.error("GraphQL: Runtime error fetching repo data: {}", e.getMessage());
			throw new GraphQLException("Error fetching repository data: " + e.getMessage());
		} catch (Exception e) {
			logger.error("GraphQL: Unexpected error fetching repo data: {}", e.getMessage());
			throw new GraphQLException("Unexpected error: " + e.getMessage());
		}
	}

	/**
	 * Custom exception for GraphQL error responses
	 */
	@Setter
	@Getter
	public static class GraphQLException extends RuntimeException {
		private final String error;
		private final long timestamp;

		public GraphQLException(String error) {
			super(error);
			this.error = error;
			this.timestamp = System.currentTimeMillis();
		}
	}
}