package com.example.vc_data_fetcher.service;

import com.example.vc_data_fetcher.dto.*;
import com.example.vc_data_fetcher.model.VCToken;
import com.example.vc_data_fetcher.repository.VCTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VCDataService {

	private static final Logger logger = LoggerFactory.getLogger(VCDataService.class);

	@Autowired
	private VCTokenRepository vcTokenRepository;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";

	// GraphQL query to check repository access
	private static final String REPO_ACCESS_QUERY = """
        query($owner: String!, $name: String!) {
          repository(owner: $owner, name: $name) {
            name
            isPrivate
            owner {
              login
            }
          }
        }
        """;

	// GraphQL query to get repository commits (we'll extract contributors from commits)
	private static final String REPOSITORY_COMMITS_QUERY = """
        query($owner: String!, $name: String!, $after: String) {
          repository(owner: $owner, name: $name) {
            defaultBranchRef {
              target {
                ... on Commit {
                  history(first: 100, after: $after) {
                    pageInfo {
                      hasNextPage
                      endCursor
                    }
                    nodes {
                      oid
                      message
                      committedDate
                      author {
                        user {
                          login
                          url
                        }
                        name
                        email
                      }
                      additions
                      deletions
                      changedFilesIfAvailable
                    }
                  }
                }
              }
            }
          }
        }
        """;

	/**
	 * Extract owner and repo from GitHub URL
	 */
	private String[] extractOwnerAndRepo(String repoUrl) {
		logger.debug("Extracting owner and repo from URL: {}", repoUrl);
		Pattern pattern = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)(?:\\.git)?/?$");
		Matcher matcher = pattern.matcher(repoUrl.trim());

		if (matcher.matches()) {
			return new String[]{matcher.group(1), matcher.group(2)};
		}
		logger.warn("Invalid GitHub repository URL: {}", repoUrl);
		throw new IllegalArgumentException("Invalid GitHub repository URL: " + repoUrl);
	}

	/**
	 * Get access token for user
	 */
	private String getAccessToken(Long userId) {
		logger.debug("Fetching access token for user ID: {}", userId);
		Optional<VCToken> tokenOpt = vcTokenRepository.findByUserId(userId);
		if (tokenOpt.isEmpty()) {
			logger.error("Access token not found for user: {}", userId);
			throw new RuntimeException("Access token not found for user: " + userId);
		}
		return tokenOpt.get().getAccess_token();
	}

	/**
	 * Create HTTP headers for GraphQL requests
	 */
	private HttpHeaders createGraphQLHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + accessToken);  // ✅ Important
		headers.set("Accept", "application/vnd.github+json");
		return headers;
	}

	/**
	 * Execute GraphQL query
	 */
	private JsonNode executeGraphQLQuery(String query, Map<String, Object> variables, String accessToken) {
		try {
			logger.debug("Executing GraphQL query with variables: {}", variables);
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("query", query);
			requestBody.put("variables", variables);

			HttpHeaders headers = createGraphQLHeaders(accessToken);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

			ResponseEntity<String> response = restTemplate.exchange(
					GITHUB_GRAPHQL_URL, HttpMethod.POST, entity, String.class
			);
			logger.debug("GraphQL query response status: {}", response.getStatusCode());

			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode responseNode = objectMapper.readTree(response.getBody());

				if (responseNode.has("errors")) {
					logger.error("GraphQL errors detected in response: {}", responseNode.get("errors").toString());
					throw new RuntimeException("GraphQL errors: " + responseNode.get("errors").toString());
				}

				return responseNode.get("data");
			} else {
				logger.error("Failed to execute GraphQL query with status: {}", response.getStatusCode());
				throw new RuntimeException("Failed to execute GraphQL query: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error executing GraphQL query: {}", e.getMessage(), e);
			throw new RuntimeException("Error executing GraphQL query: " + e.getMessage(), e);
		}
	}

	/**
	 * Check if user has access to read the repository
	 */
	public ResponseEntity checkRepoAccess(String repoUrl, Long userId) {
		logger.info("Checking repository access for URL: {} by user ID: {}", repoUrl, userId);
		try {
			String[] ownerRepo = extractOwnerAndRepo(repoUrl);
			String owner = ownerRepo[0];
			String repo = ownerRepo[1];
			String accessToken = getAccessToken(userId);

			Map<String, Object> variables = new HashMap<>();
			variables.put("owner", owner);
			variables.put("name", repo);

			try {
				JsonNode data = executeGraphQLQuery(REPO_ACCESS_QUERY, variables, accessToken);
				logger.info("Repository access confirmed for repo: {}", repoUrl);
				return ResponseEntity.status(HttpStatus.OK).build();
			} catch (RuntimeException e) {
				if (e.getMessage().contains("Could not resolve")) {
					logger.warn("Repository {} not resolvable or unauthorized.", repoUrl);
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
				}
				throw e;
			}
		} catch (Exception e) {
			logger.error("Error checking repository access for URL {}: {}", repoUrl, e.getMessage());
			throw new RuntimeException("Error checking repository access: " + e.getMessage(), e);
		}
	}

	/**
	 * Get all contributors with their commits using GraphQL
	 */
	public List<ContributorWithCommits> getContributorsWithCommits(String repoUrl, Long userId) {
		logger.info("Fetching contributors and commits for repo: {} by user ID: {}", repoUrl, userId);
		try {
			String[] ownerRepo = extractOwnerAndRepo(repoUrl);
			String owner = ownerRepo[0];
			String repo = ownerRepo[1];
			String accessToken = getAccessToken(userId);

			// Get contributors
			List<Contributor> contributors = getContributorsGraphQL(owner, repo, accessToken);

			// Get commits for each contributor
			List<ContributorWithCommits> result = new ArrayList<>();

			for (Contributor contributor : contributors) {
				try {
					logger.debug("Fetching commits for contributor: {}", contributor.getAuthorName());
					List<Commit> commits = getCommitsByAuthorGraphQL(owner, repo, contributor.getAuthorName(), accessToken);

					ContributorWithCommits cwc = new ContributorWithCommits(
							contributor.getAuthorName(),
							contributor.getGithubUrl(),
							contributor.getContributions(),
							commits
					);
					result.add(cwc);

				} catch (Exception e) {
					System.err.println("Error fetching commits for contributor " + contributor.getAuthorName() + ": " + e.getMessage());
					logger.error("Error fetching commits for contributor {}: {}", contributor.getAuthorName(), e.getMessage());
					// Add contributor with empty commits list if commits fetch fails
					ContributorWithCommits cwc = new ContributorWithCommits(
							contributor.getAuthorName(),
							contributor.getGithubUrl(),
							contributor.getContributions(),
							new ArrayList<>()
					);
					result.add(cwc);
				}
			}
			logger.info("Successfully fetched contributor and commit data for repo: {}", repoUrl);
			return result;
		} catch (Exception e) {
			logger.error("Error fetching contributors with commits for repo {}: {}", repoUrl, e.getMessage(), e);
			throw new RuntimeException("Error fetching contributors with commits: " + e.getMessage(), e);
		}
	}

	/**
	 * Get contributors using GraphQL by extracting unique authors from commits
	 */
	private List<Contributor> getContributorsGraphQL(String owner, String repo, String accessToken) {
		try {
			logger.debug("Starting pagination for contributors in {}/{}", owner, repo);
			Map<String, Contributor> contributorsMap = new HashMap<>();
			String cursor = null;
			boolean hasNextPage = true;

			while (hasNextPage) {
				Map<String, Object> variables = new HashMap<>();
				variables.put("owner", owner);
				variables.put("name", repo);
				if (cursor != null) {
					variables.put("after", cursor);
					logger.debug("Fetching next page of commits using cursor: {}", cursor);
				}

				JsonNode data = executeGraphQLQuery(REPOSITORY_COMMITS_QUERY, variables, accessToken);
				JsonNode repository = data.get("repository");

				if (repository != null && !repository.isNull()) {
					JsonNode defaultBranch = repository.get("defaultBranchRef");
					if (defaultBranch != null) {
						JsonNode target = defaultBranch.get("target");
						JsonNode history = target.get("history");
						JsonNode nodes = history.get("nodes");
						JsonNode pageInfo = history.get("pageInfo");

						// Extract contributors from commits
						for (JsonNode commitNode : nodes) {
							JsonNode author = commitNode.get("author");
							if (author != null) {
								JsonNode user = author.get("user");
								String login = null;
								String url = null;

								if (user != null && !user.isNull()) {
									login = user.get("login").asText();
									url = user.get("url").asText();
								} else {
									// Fallback to author name if user is null
									login = author.get("name") != null ? author.get("name").asText() : "Unknown";
									url = "https://github.com/" + login;
								}

								if (login != null) {
									contributorsMap.putIfAbsent(login, new Contributor(login, url, 0));
									// Increment contribution count
									Contributor contributor = contributorsMap.get(login);
									contributorsMap.put(login, new Contributor(login, url, contributor.getContributions() + 1));
								}
							}
						}

						hasNextPage = pageInfo.get("hasNextPage").asBoolean();
						if (hasNextPage) {
							cursor = pageInfo.get("endCursor").asText();
						}
					} else {
						logger.warn("Repository {}/{} has no default branch ref.", owner, repo);
						break;
					}
				} else {
					logger.warn("Repository {}/{} data not found or is null.", owner, repo);
					break;
				}
			}
			logger.info("Finished fetching contributors. Total found: {}", contributorsMap.size());
			return new ArrayList<>(contributorsMap.values());
		} catch (Exception e) {
			logger.error("Error fetching contributors for {}/{}: {}", owner, repo, e.getMessage(), e);
			throw new RuntimeException("Error fetching contributors: " + e.getMessage(), e);
		}
	}

	/**
	 * Get commits by author using GraphQL
	 */
	private List<Commit> getCommitsByAuthorGraphQL(String owner, String repo, String authorLogin, String accessToken) {
		try {
			logger.debug("Starting commit fetching for author: {} in {}/{}", authorLogin, owner, repo);
			List<Commit> allCommits = new ArrayList<>();
			String cursor = null;
			boolean hasNextPage = true;

			while (hasNextPage) {
				Map<String, Object> variables = new HashMap<>();
				variables.put("owner", owner);
				variables.put("name", repo);
				if (cursor != null) {
					variables.put("after", cursor);
				}

				JsonNode data = executeGraphQLQuery(REPOSITORY_COMMITS_QUERY, variables, accessToken);
				JsonNode repository = data.get("repository");

				if (repository != null && !repository.isNull()) {
					JsonNode defaultBranch = repository.get("defaultBranchRef");
					if (defaultBranch != null) {
						JsonNode target = defaultBranch.get("target");
						JsonNode history = target.get("history");
						JsonNode nodes = history.get("nodes");
						JsonNode pageInfo = history.get("pageInfo");

						for (JsonNode commitNode : nodes) {
							JsonNode author = commitNode.get("author");
							if (author != null) {
								String commitAuthor = null;
								JsonNode user = author.get("user");

								if (user != null && !user.isNull()) {
									commitAuthor = user.get("login").asText();
								} else {
									commitAuthor = author.get("name") != null ? author.get("name").asText() : "Unknown";
								}

								// Filter commits by author
								if (authorLogin.equals(commitAuthor)) {
									String sha = commitNode.get("oid").asText();
									String message = commitNode.get("message").asText();
									String date = commitNode.get("committedDate").asText();

									// Get comprehensive file changes for this commit
									List<FileData> files = getCommitFilesWithPatchData(owner, repo, sha, accessToken);

									Commit commit = new Commit();
									commit.setSha(sha);
									commit.setMsg(message);
									commit.setDate(date);
									commit.setFiles(files);

									allCommits.add(commit);
								}
							}
						}

						hasNextPage = pageInfo.get("hasNextPage").asBoolean();
						if (hasNextPage) {
							cursor = pageInfo.get("endCursor").asText();
						}
					} else {
						break;
					}
				} else {
					break;
				}
			}
			logger.info("Finished fetching commits for author {}. Total: {}", authorLogin, allCommits.size());
			return allCommits;
		} catch (Exception e) {
			logger.error("Error fetching commits for author {}: {}", authorLogin, e.getMessage(), e);
			throw new RuntimeException("Error fetching commits: " + e.getMessage(), e);
		}
	}

	/**
	 * Get comprehensive file details with all patch data for a commit using REST API
	 */
	private List<FileData> getCommitFilesWithPatchData(String owner, String repo, String sha, String accessToken) {
		logger.debug("Fetching file patch data for commit SHA: {}", sha);
		try {
			String commitUrl = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repo, sha);

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken);
			headers.set("Accept", "application/vnd.github+json");
			headers.set("X-GitHub-Api-Version", "2022-11-28");

			HttpEntity<?> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(commitUrl, HttpMethod.GET, entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				JsonNode commitDetail = objectMapper.readTree(response.getBody());
				JsonNode filesArray = commitDetail.get("files");

				List<FileData> fileDataList = new ArrayList<>();

				if (filesArray != null && filesArray.isArray()) {
					for (JsonNode fileNode : filesArray) {
						FileData fileData = new FileData();

						// Get full filename path
						String fullFilename = fileNode.get("filename").asText();
						String fileName = fullFilename.substring(fullFilename.lastIndexOf('/') + 1);
						fileData.setFileName(fileName);
						fileData.setFullPath(fullFilename); // Store full path

						// Extract file extension
						int dotIndex = fileName.lastIndexOf('.');
						String extension = dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
						fileData.setExtension(extension);

						// Get file operation status (added, modified, deleted, renamed)
						String status = fileNode.get("status").asText();
						fileData.setOperation(status);

						// Get the patch/code changes - this is the key data
						String patchData = "";
						if (fileNode.has("patch") && !fileNode.get("patch").isNull()) {
							patchData = fileNode.get("patch").asText();
						} else {
							// Handle special cases
							switch (status) {
								case "added":
									patchData = "// New file added";
									break;
								case "removed":
									patchData = "// File deleted";
									break;
								case "renamed":
									String previousFilename = fileNode.has("previous_filename") ?
											fileNode.get("previous_filename").asText() : "unknown";
									patchData = "// File renamed from: " + previousFilename + " to: " + fullFilename;
									break;
								default:
									patchData = "// No patch data available";
							}
						}
						fileData.setCode(patchData);

						// Get comprehensive file statistics
						fileData.setAdditions(fileNode.has("additions") ? fileNode.get("additions").asInt() : 0);
						fileData.setDeletions(fileNode.has("deletions") ? fileNode.get("deletions").asInt() : 0);
						fileData.setChanges(fileNode.has("changes") ? fileNode.get("changes").asInt() :
								(fileData.getAdditions() + fileData.getDeletions()));

						// Additional metadata
						fileData.setBinaryFile(fileNode.has("binary") ? fileNode.get("binary").asBoolean() : false);
						if (fileNode.has("previous_filename")) {
							fileData.setPreviousFilename(fileNode.get("previous_filename").asText());
						}

						fileDataList.add(fileData);

						// Debug logging
						System.out.println(String.format(
								"File: %s, Status: %s, Patch length: %d chars, +%d -%d",
								fileName, status, patchData.length(),
								fileData.getAdditions(), fileData.getDeletions()
						));
						logger.debug("Processed file: {}, Status: {}, Additions: {}, Deletions: {}", fileName, status, fileData.getAdditions(), fileData.getDeletions());
					}
				}

				return fileDataList;
			} else {
				System.err.println("Failed to fetch commit details. Status: " + response.getStatusCode());
				logger.error("Failed to fetch commit details for SHA {}. Status: {}", sha, response.getStatusCode());
				return new ArrayList<>();
			}

		} catch (Exception e) {
			System.err.println("Error fetching file details for commit " + sha + ": " + e.getMessage());
			e.printStackTrace();
			logger.error("Error fetching file details for commit {}: {}", sha, e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * Legacy method - kept for backward compatibility
	 */
	private List<FileData> getCommitFilesGraphQL(String owner, String repo, JsonNode commitNode, String accessToken) {
		String sha = commitNode.get("oid").asText();
		return getCommitFilesWithPatchData(owner, repo, sha, accessToken);
	}

	// Legacy methods for backward compatibility
	public ContributorsResponse getContributors(String repoUrl, Long userId) {
		logger.info("LEGACY: Fetching contributors for repo: {} by user ID: {}", repoUrl, userId);
		try {
			String[] ownerRepo = extractOwnerAndRepo(repoUrl);
			String owner = ownerRepo[0];
			String repo = ownerRepo[1];
			String accessToken = getAccessToken(userId);

			List<Contributor> contributors = getContributorsGraphQL(owner, repo, accessToken);
			return new ContributorsResponse(contributors);
		} catch (Exception e) {
			logger.error("LEGACY: Error fetching contributors for repo {}: {}", repoUrl, e.getMessage(), e);
			throw new RuntimeException("Error fetching contributors: " + e.getMessage(), e);
		}
	}

	public CommitsResponse getCommitsByAuthor(String repoUrl, String authorName, Long userId) {
		logger.info("LEGACY: Fetching commits for author: {} in repo: {} by user ID: {}", authorName, repoUrl, userId);
		try {
			String[] ownerRepo = extractOwnerAndRepo(repoUrl);
			String owner = ownerRepo[0];
			String repo = ownerRepo[1];
			String accessToken = getAccessToken(userId);

			List<Commit> commits = getCommitsByAuthorGraphQL(owner, repo, authorName, accessToken);
			return new CommitsResponse(commits, authorName, commits.size());
		} catch (Exception e) {
			logger.error("LEGACY: Error fetching commits for author {} in repo {}: {}", authorName, repoUrl, e.getMessage(), e);
			throw new RuntimeException("Error fetching commits: " + e.getMessage(), e);
		}
	}
}