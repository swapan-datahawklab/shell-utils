package com.example.shelldemo.monitoring;

import com.example.shelldemo.monitoring.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;

@Component
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;
    
    @Value("${notification.email.to:admin@example.com}")
    private String toEmail;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(Alert alert) {
        log.warn("Alert triggered: {}", alert.getMessage());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Alert: " + alert.getEvent().getName());
        message.setText(alert.getMessage() + "\n\nMetric Details:\n" + 
            "Value: " + alert.getEvent().getValue() + "\n" +
            "Timestamp: " + alert.getEvent().getTimestamp());
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send alert email", e);
        }
    }
} 