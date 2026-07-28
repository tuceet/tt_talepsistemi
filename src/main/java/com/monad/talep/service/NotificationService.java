package com.monad.talep.service;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.NotificationEntity;
import com.monad.talep.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifRepo;

    public NotificationService(NotificationRepository notifRepo) { this.notifRepo = notifRepo; }

    public void notify(AppUser user, String message) {
        NotificationEntity n = new NotificationEntity();
        n.setUser(user);
        n.setMessage(message);
        notifRepo.save(n);
    }

    public List<NotificationEntity> forUser(AppUser user) {
        return notifRepo.findByUserOrderByCreatedAtDesc(user);
    }

    public long unreadCount(AppUser user) { return notifRepo.countByUserAndReadFalse(user); }

    public void markRead(NotificationEntity n) {
        n.setRead(true);
        notifRepo.save(n);
    }
}
