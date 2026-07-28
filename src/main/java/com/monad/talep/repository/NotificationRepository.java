package com.monad.talep.repository;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserOrderByCreatedAtDesc(AppUser user);
    long countByUserAndReadFalse(AppUser user);
}
