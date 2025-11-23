package com.example.dev_impact.service;

import com.example.dev_impact.constant.Message;
import com.example.dev_impact.model.AnalysisStatus;
import com.example.dev_impact.model.CodeAnalysis;
import com.example.dev_impact.model.User;
import com.example.dev_impact.repository.CodeAnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CodeAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(CodeAnalysisService.class);

    private CodeAnalysisRepository codeAnalysisRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${version_control_service.url}")
    private String vcsServiceUrl;

    @Value("${code_analyzer_service.url}")
    private String analyzerServiceUrl;

    @Value("${application.url}")
    private String appUrl;

    @Value("${email_service.url}")
    private String emailServiceUrl;

    @Value("${email_sender_address}")
    private String emailSenderAddress;

    public CodeAnalysisService(CodeAnalysisRepository codeAnalysisRepository) {
        this.codeAnalysisRepository = codeAnalysisRepository;
    }

    public HashMap<String, String> startAnalyzingCode(String repoUrl, User user) {
        logger.info("Starting analysis request for repo: {} by user: {}", repoUrl, user.getUsername());
        HashMap<String, String> response = new HashMap<>();
        try {
            if (!isValidRepo(repoUrl, user)) {
                logger.warn("Repository validation failed for repo: {}", repoUrl);
                response.put("status", "error");
                response.put("message", Message.INVALID_REPO_URL);
                return response;
            }

            CodeAnalysis codeAnalysis = new CodeAnalysis();
            codeAnalysis.setRepoUrl(repoUrl);
            codeAnalysis.setUser(user);
            codeAnalysis.setStatus(AnalysisStatus.IN_PROGRESS);
            codeAnalysisRepository.save(codeAnalysis);
            logger.info("Created new CodeAnalysis entry with ID: {}", codeAnalysis.getId());

            requestCodeAnalysis(repoUrl, codeAnalysis);

            response.put("status", "success");
            response.put("message", Message.ANALYSIS_IN_PROGRESS);
            return response;
        } catch (Exception e) {
            logger.error("Analysis failed to start for repo: {}. Error: {}", repoUrl, e.getMessage(), e);
            response.put("status", "error");
            response.put("message", Message.ANALYSIS_FAILED);
            return response;
        }
    }

    @Transactional
    public boolean handleAnalysisCallback(Long analysisId, JsonNode result) {

        try {
            CodeAnalysis codeAnalysis = codeAnalysisRepository.findById(analysisId).orElseThrow();
            codeAnalysis.setStatus(AnalysisStatus.COMPLETED);
            codeAnalysis.setResult(result);
            codeAnalysisRepository.save(codeAnalysis);
            logger.info("Analysis ID {} updated to COMPLETED.", analysisId);

            sendEmailNotification(
                    codeAnalysis.getUser(),
                    Message.ANALYSIS_COMPLETED_EMAIL_SUBJECT,
                    Message.ANALYSIS_COMPLETED_EMAIL_BODY.replace("{repoUrl}", codeAnalysis.getRepoUrl())
            );

            return true;

        } catch (Exception e) {
            logger.error("Failed to handle analysis callback for ID {}: {}", analysisId, e.getMessage(), e);
            return false;
        }
    }


    public List<CodeAnalysis> getAllAnalysesForUser(User user) {
        logger.debug("Fetching all analyses for user ID: {}", user.getId());
        return codeAnalysisRepository.findByUserId(user.getId());
    }

    private void sendEmailNotification(User user, String subject, String body) {
        logger.info("Attempting to send email notification to {} for subject: {}", user.getUsername(), subject);
        try {
            String boundary = "----Boundary" + System.currentTimeMillis();

            // Build multipart/form-data body
            String formData =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"fromAddress\"\r\n\r\n" +
                            emailSenderAddress + "\r\n" +
                            "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"toAddress\"\r\n\r\n" +
                            user.getUsername() + "\r\n" +
                            "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"subject\"\r\n\r\n" +
                            subject + "\r\n" +
                            "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"body\"\r\n\r\n" +
                            body + "\r\n" +
                            "--" + boundary + "--\r\n";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(emailServiceUrl + "/api/v1.0.0/emails"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Email API Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidRepo(String repoUrl, User user) {
        logger.debug("Validating repository URL: {} for user ID: {}", repoUrl, user.getId());
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(vcsServiceUrl + "/api/v1.0.0/repo-validate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"repoUrl\":\"" + repoUrl + "\", \"userId\":\"" + user.getId() + "\"}"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Repo validation response code: {}", response.statusCode());
            return response.statusCode() == 200;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Repo validation failed due to connection error: {}", e.getMessage());
            return false;
        }
    }

    private void requestCodeAnalysis(String repoUrl, CodeAnalysis codeAnalysis) {
        logger.info("Requesting analysis from external service for ID: {}", codeAnalysis.getId());
        try {
            // Build the request payload dynamically
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("repo_url", repoUrl);
            requestBody.put("call_back_url", appUrl + "/api/v1.0.0/analyze/callback/" + codeAnalysis.getId());
            requestBody.put("user_id", codeAnalysis.getUser().getId());

            // Convert the map to JSON using Jackson
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestBody);

            // Build the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(analyzerServiceUrl + "/v1.0.0/initiate/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send the request and log the response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("✅ Code analysis request successful for ID {}. Response: {}",
                        codeAnalysis.getId(), response.body());
            } else {
                logger.warn("⚠️ Code analysis request returned status {} for ID {}. Response: {}",
                        response.statusCode(), codeAnalysis.getId(), response.body());
            }

        } catch (IOException | InterruptedException e) {
            logger.error("❌ Failed to request code analysis for ID {}: {}", codeAnalysis.getId(), e.getMessage(), e);
            Thread.currentThread().interrupt(); // good practice when catching InterruptedException
        }
    }

    public CodeAnalysis getCodeAnalysisById(Long id) throws Exception {
        logger.debug("Fetching CodeAnalysis by ID: {}", id);
        return codeAnalysisRepository.findById(id).orElseThrow(() -> new Exception("CodeAnalysis not found with id: " + id));
    }
}



