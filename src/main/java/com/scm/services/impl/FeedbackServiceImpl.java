package com.scm.services.impl;

import com.scm.entities.Feedback;
import com.scm.entities.User;
import com.scm.repsitories.FeedbackRepo;
import com.scm.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepo feedbackRepo;

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepo.save(feedback);
    }

    @Override
    public List<Feedback> getFeedbacksByUser(User user) {
        return feedbackRepo.findByUserOrderBySubmissionTimeDesc(user);
    }
}
