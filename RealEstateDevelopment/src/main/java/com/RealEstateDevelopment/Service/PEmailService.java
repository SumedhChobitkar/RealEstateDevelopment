package com.RealEstateDevelopment.Service;

import org.springframework.mail.SimpleMailMessage;

public interface PEmailService {
    void sendEmail(String to, String subject, String body);

    void sendEmail(SimpleMailMessage mailMessage);
}
