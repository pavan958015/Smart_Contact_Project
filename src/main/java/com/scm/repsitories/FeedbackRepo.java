package com.scm.repsitories;

import com.scm.entities.Feedback;
import com.scm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepo extends JpaRepository<Feedback, String> {
    List<Feedback> findByUserOrderBySubmissionTimeDesc(User user);
}
