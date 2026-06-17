package com.scm.services;

public interface EmailService {

    //
    void sendEmail(String to, String subject, String body);

    void sendEmail(String from, String to, String subject, String body);

    void sendEmail(String from, String replyTo, String to, String subject, String body);

    //
    void sendEmailWithHtml();

    //
    void sendEmailWithAttachment();

}
