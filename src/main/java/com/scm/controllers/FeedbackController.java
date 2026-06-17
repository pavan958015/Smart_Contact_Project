package com.scm.controllers;

import com.scm.entities.Feedback;
import com.scm.entities.User;
import com.scm.forms.FeedbackForm;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.FeedbackService;
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
@RequestMapping("/user/feedback")
public class FeedbackController {

    private Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showFeedbackForm(Model model, Authentication authentication) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        FeedbackForm feedbackForm = new FeedbackForm();
        feedbackForm.setRating(5); // default rating

        model.addAttribute("feedbackForm", feedbackForm);
        model.addAttribute("feedbacks", feedbackService.getFeedbacksByUser(user));
        return "user/feedback";
    }

    @PostMapping
    public String submitFeedback(@Valid @ModelAttribute FeedbackForm feedbackForm, BindingResult result,
                                 Authentication authentication, HttpSession session, Model model) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        if (result.hasErrors()) {
            logger.info("Feedback validation errors: {}", result.getAllErrors());
            model.addAttribute("feedbacks", feedbackService.getFeedbacksByUser(user));
            return "user/feedback";
        }

        Feedback feedback = Feedback.builder()
                .id(UUID.randomUUID().toString())
                .subject(feedbackForm.getSubject())
                .message(feedbackForm.getMessage())
                .rating(feedbackForm.getRating())
                .submissionTime(LocalDateTime.now())
                .user(user)
                .build();

        feedbackService.saveFeedback(feedback);

        session.setAttribute("message", Message.builder()
                .content("Thank you! Your feedback has been recorded successfully.")
                .type(MessageType.green)
                .build());

        return "redirect:/user/feedback";
    }
}
