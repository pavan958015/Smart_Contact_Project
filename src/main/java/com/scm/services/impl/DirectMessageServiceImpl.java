package com.scm.services.impl;

import com.scm.entities.DirectMessage;
import com.scm.entities.User;
import com.scm.repsitories.DirectMessageRepo;
import com.scm.services.DirectMessageService;
import com.scm.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DirectMessageServiceImpl implements DirectMessageService {

    @Autowired
    private DirectMessageRepo directMessageRepo;

    @Autowired
    private EmailService emailService;

    @Override
    public DirectMessage saveMessage(DirectMessage message) {
        return directMessageRepo.save(message);
    }

    @Override
    public List<DirectMessage> getMessagesByUser(User user) {
        return directMessageRepo.findByUserOrderBySentTimeDesc(user);
    }

    @Override
    public void sendMessageEmail(String to, String subject, String body) {
        emailService.sendEmail(to, subject, body);
    }
}
