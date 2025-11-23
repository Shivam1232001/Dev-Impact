package com.example.email_service.mail;

import com.example.email_service.model.EmailEntity;

public interface MailSenderAdapter {

    void sendEmail(EmailEntity emailEntity) throws Exception;
    
}
