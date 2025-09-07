package com.example.calendar.service;

import com.example.calendar.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class CompanyEmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendInvitation(Company company, String toEmail, String token) {
        JavaMailSender companiesMailSender = createJavaMailSender(company);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(company.getWorkEmail());
        message.setSubject("Приглашение в систему");
        message.setText("Вы приглашены! Перейдите по ссылке: http://localhost:8080/lander?token=" + token);

        companiesMailSender.send(message);
    }

    private JavaMailSender createJavaMailSender(Company company) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setPort(company.getSmtpPort());
        mailSender.setHost(company.getSmtpHost());
        mailSender.setUsername(company.getWorkEmail());
        mailSender.setPassword(company.getEmailPassword());


        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}
