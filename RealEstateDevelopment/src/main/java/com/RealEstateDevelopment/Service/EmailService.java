package com.RealEstateDevelopment.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}