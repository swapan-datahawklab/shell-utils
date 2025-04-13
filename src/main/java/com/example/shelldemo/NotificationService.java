package com.example.shelldemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import com.example.shelldemo.monitoring.Alert;

public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Session session;
    private final String fromAddress;
    private final String toAddress;

    public NotificationService(String host, int port, String username, String password, 
                             String fromAddress, String toAddress) {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendAlert(Alert alert) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject("Alert: " + alert.getRule().getName());
            message.setText(String.format(
                "Alert triggered for metric: %s%nValue: %f%nThreshold: %f%nTimestamp: %s",
                alert.getEvent().getName(),
                alert.getEvent().getValue(),
                alert.getRule().getThreshold(),
                alert.getEvent().getTimestamp()
            ));
            Transport.send(message);
            log.info("Sent alert notification: {}", alert);
        } catch (MessagingException e) {
            log.error("Failed to send alert notification: {}", alert, e);
        }
    }
} 