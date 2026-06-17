package com.scm.repsitories;

import com.scm.entities.DirectMessage;
import com.scm.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DirectMessageRepo extends JpaRepository<DirectMessage, String> {
    List<DirectMessage> findByUserOrderBySentTimeDesc(User user);
}
