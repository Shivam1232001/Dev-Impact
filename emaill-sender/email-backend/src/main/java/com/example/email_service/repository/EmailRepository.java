package com.example.email_service.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.email_service.model.EmailEntity;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

}