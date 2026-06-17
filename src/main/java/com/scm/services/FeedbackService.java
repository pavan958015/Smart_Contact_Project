package com.scm.services;

import com.scm.entities.Feedback;
import com.scm.entities.User;
import java.util.List;

public interface FeedbackService {
    Feedback saveFeedback(Feedback feedback);
    List<Feedback> getFeedbacksByUser(User user);
}
