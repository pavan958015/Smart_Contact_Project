package com.scm.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.entities.Contact;
import com.scm.forms.ContactMessageForm;
import com.scm.services.ContactService;
import com.scm.services.EmailService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String supportEmail;

    @GetMapping("/contacts/{contactId}")
    public Contact getContact(@PathVariable String contactId) {
        return contactService.getById(contactId);
    }

    @PostMapping("/contact")
    public ResponseEntity<?> handleContactSubmit(@RequestBody ContactMessageForm form) {
        String emailBody = "You have received a new support inquiry from the SCM 2.0 Contact page.\n\n" +
                           "Sender Details:\n" +
                           "Name: " + form.getName() + "\n" +
                           "Email: " + form.getEmail() + "\n" +
                           "Subject: " + form.getSubject() + "\n\n" +
                           "Message Content:\n" +
                           form.getMessage();

        try {
            // Send email to support email, from support email, with Reply-To set to the sender's email
            emailService.sendEmail(form.getName() + " <" + form.getEmail() + ">", form.getEmail(), supportEmail, "[SCM Support Request] " + form.getSubject(), emailBody);
            return ResponseEntity.ok(Map.of("success", true, "message", "Your message has been sent successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("success", false, "message", "Failed to send message: " + e.getMessage()));
        }
    }
}
