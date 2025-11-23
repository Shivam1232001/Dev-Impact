package com.example.email_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class EmailEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String toAddress;
    private String fromAddress;
    private String subject;
    private String body;
    private String status;
    private String attachmentName;
    private LocalDateTime sentAt;
}
