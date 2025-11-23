package com.example.email_service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.example.email_service.model.EmailEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component("gmailAdapter")
public class GmailAdapter implements MailSenderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GmailAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String defaultFromAddress;

    public GmailAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(EmailEntity email) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        boolean hasAttachment = email.getAttachmentName() != null && !email.getAttachmentName().isEmpty();
        MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachment);
    
        String[] recipients = email.getToAddress().split(",");
        helper.setTo(recipients);
        helper.setSubject(email.getSubject());
        helper.setText(email.getBody());
        helper.setFrom(email.getFromAddress() !=null && !email.getFromAddress().isEmpty() ? email.getFromAddress() : defaultFromAddress);

        if(hasAttachment) {
            try{
                FileSystemResource file  = new FileSystemResource(email.getAttachmentName());
                helper.addAttachment(new java.io.File(email.getAttachmentName()).getName(), file);
                logger.info("Attachement added: {}", email.getAttachmentName());
            }catch (Exception e){
                logger.warn("Failed to add attachment {}: {}", email.getAttachmentName(), e.getMessage());
                throw new MessagingException("Attachment error: " + e.getMessage());
            }
        }

        mailSender.send(message);
        logger.info("Sent via GmailAdapter to {}", email.getToAddress());
    }
    
}
