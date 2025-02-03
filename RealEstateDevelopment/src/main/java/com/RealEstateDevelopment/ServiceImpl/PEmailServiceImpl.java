package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Service.PEmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

@Service
public class PEmailServiceImpl implements PEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);  // The "true" means HTML content is supported.

            mailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send email to " + to);
        }
    }

    @Override
    public void sendEmail(SimpleMailMessage mailMessage) {
        try {
            mailSender.send(mailMessage);
            System.out.println("Email sent successfully to " + mailMessage.getTo()[0]);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send email to " + mailMessage.getTo()[0]);
        }
    }
}
