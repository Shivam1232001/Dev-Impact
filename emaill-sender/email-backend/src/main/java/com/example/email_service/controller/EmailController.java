package com.example.email_service.controller;

import com.example.email_service.model.EmailEntity;
import com.example.email_service.service.EmailService;

import lombok.Data;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0.0/emails")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return emailService.healthCheck();
    }

    @GetMapping
    public List<EmailEntity> getAllEmails() {
        return emailService.getAllEmails();
    }

    @PostMapping(consumes = "multipart/form-data")
    public String sendEmail(
            @RequestParam("fromAddress") String fromAddress,
            @RequestParam("toAddress") List<String> toAddress,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) {

        try {
            String attachmentPath = null;

            // Save the file if an attachment is provided
            if (attachment != null && !attachment.isEmpty()) {
                Path uploadDir = Paths.get("uploads"); // Directory to save files
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                attachmentPath = uploadDir.resolve(attachment.getOriginalFilename()).toString();
                attachment.transferTo(Paths.get(attachmentPath));
            }

            // Call the email service with the file path
            String result = emailService.sendEmail(
                    fromAddress,
                    toAddress,
                    subject,
                    body,
                    attachmentPath
            );

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Failed to send email. " + e.getMessage();
        }
    }

    @Data
    public static class EmailRequest {
        public String fromAddress;
        public List<String> toAddress;
        public String subject;
        public String body;
        public String attachmentName;
    }
}