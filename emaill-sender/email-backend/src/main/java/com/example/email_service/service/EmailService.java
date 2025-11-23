package com.example.email_service.service;

import com.example.email_service.repository.EmailRepository;
import com.example.email_service.model.EmailEntity;
import com.example.email_service.mail.MailSenderAdapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String defaultFromAddress;

    private final EmailRepository emailRepository;
    private MailSenderAdapter mailSenderAdapter;

    public EmailService(EmailRepository emailRepository, MailSenderAdapter mailSenderAdapter) {
        this.emailRepository = emailRepository;
        this.mailSenderAdapter = mailSenderAdapter;
    }

    public String healthCheck() {
        return "Email Service is up and running!";
    }

    public List<EmailEntity> getAllEmails() {
        return emailRepository.findAll();
    }

    public String sendEmail(String from, List<String> to, String subject, String body, String attachmentName) {

        EmailEntity emailEntity = new EmailEntity();
        emailEntity.setFromAddress(defaultFromAddress);
        emailEntity.setToAddress(String.join(",", to));
        emailEntity.setSubject(subject);
        emailEntity.setBody(body);
        emailEntity.setAttachmentName(attachmentName);
        emailEntity.setStatus("PENDING");
        emailEntity.setSentAt(LocalDateTime.now());
        emailRepository.save(emailEntity);
        try{
            mailSenderAdapter.sendEmail(emailEntity);
            emailEntity.setStatus("SENT");
            emailRepository.save(emailEntity);
            return "Email sent successfully to " + to;
        } catch (MailException e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage());
            emailEntity.setStatus("FAILED");
            emailRepository.save(emailEntity);
            return "Failed to send email: " + e.getMessage();
        } catch (Exception e){
            logger.error("Unexpected error sending email to {}: {}", to, e.getMessage());
            emailEntity.setStatus("FAILED");
            emailRepository.save(emailEntity);
            return "Unexpected error: " + e.getMessage();
        }
    }
}
