package com.scm.services;

import com.scm.entities.DirectMessage;
import com.scm.entities.User;
import java.util.List;

public interface DirectMessageService {
    DirectMessage saveMessage(DirectMessage message);
    List<DirectMessage> getMessagesByUser(User user);
    void sendMessageEmail(String to, String subject, String body);
}
