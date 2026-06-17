package com.scm.controllers;

import com.scm.entities.DirectMessage;
import com.scm.entities.User;
import com.scm.forms.DirectMessageForm;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.DirectMessageService;
import com.scm.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/user/direct-message")
public class DirectMessageController {

    private Logger logger = LoggerFactory.getLogger(DirectMessageController.class);

    @Autowired
    private DirectMessageService directMessageService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showDirectMessageForm(Model model, Authentication authentication) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        DirectMessageForm directMessageForm = new DirectMessageForm();

        model.addAttribute("directMessageForm", directMessageForm);
        model.addAttribute("contacts", user.getContacts());
        model.addAttribute("messages", directMessageService.getMessagesByUser(user));
        return "user/direct_message";
    }

    @PostMapping
    public String sendDirectMessage(@Valid @ModelAttribute DirectMessageForm directMessageForm, BindingResult result,
                                    Authentication authentication, HttpSession session, Model model) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        if (result.hasErrors()) {
            logger.info("Direct Message validation errors: {}", result.getAllErrors());
            model.addAttribute("contacts", user.getContacts());
            model.addAttribute("messages", directMessageService.getMessagesByUser(user));
            return "user/direct_message";
        }

        try {
            // 1. Send Email
            directMessageService.sendMessageEmail(
                    username,
                    directMessageForm.getRecipientEmail(),
                    directMessageForm.getSubject(),
                    directMessageForm.getBody()
            );

            // 2. Save Message Log
            DirectMessage directMessage = DirectMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .recipientEmail(directMessageForm.getRecipientEmail())
                    .subject(directMessageForm.getSubject())
                    .body(directMessageForm.getBody())
                    .sentTime(LocalDateTime.now())
                    .user(user)
                    .build();

            directMessageService.saveMessage(directMessage);

            session.setAttribute("message", Message.builder()
                    .content("Email sent successfully and logged to Outbox.")
                    .type(MessageType.green)
                    .build());

        } catch (Exception e) {
            logger.error("Failed to send direct message email", e);
            session.setAttribute("message", Message.builder()
                    .content("Failed to send email. Please check your mail configurations. Error: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
        }

        return "redirect:/user/direct-message";
    }
}
