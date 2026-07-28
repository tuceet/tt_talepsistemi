package com.monad.talep.repository;

import com.monad.talep.entity.ActivityLog;
import com.monad.talep.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();
    List<ActivityLog> findTop50ByUserOrderByCreatedAtDesc(AppUser user);
}
