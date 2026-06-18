package com.scm.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.scm.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender eMailSender;

    @Value("${spring.mail.username}")
    private String domainName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = eMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.setFrom(domainName, "Smart Contact Manager");
            eMailSender.send(mimeMessage);
        } catch (Exception e) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(domainName);
            eMailSender.send(message);
        }
    }

    @Override
    public void sendEmail(String from, String to, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = eMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            String personal = from;
            String replyTo = from;
            if (from.contains("<") && from.contains(">")) {
                personal = from.substring(0, from.indexOf("<")).trim();
                replyTo = from.substring(from.indexOf("<") + 1, from.indexOf(">")).trim();
            }

            helper.setFrom(domainName, personal);
            helper.setReplyTo(replyTo);
            eMailSender.send(mimeMessage);
        } catch (Exception e) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(domainName);
            message.setReplyTo(from);
            eMailSender.send(message);
        }
    }

    @Override
    public void sendEmail(String from, String replyTo, String to, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = eMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            String personal = from;
            if (from.contains("<") && from.contains(">")) {
                personal = from.substring(0, from.indexOf("<")).trim();
            }

            helper.setFrom(domainName, personal);
            helper.setReplyTo(replyTo);
            eMailSender.send(mimeMessage);
        } catch (Exception e) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(domainName);
            message.setReplyTo(replyTo);
            eMailSender.send(message);
        }
    }

    @Override
    public void sendEmailWithHtml() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithHtml'");
    }

    @Override
    public void sendEmailWithAttachment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithAttachment'");
    }

}
