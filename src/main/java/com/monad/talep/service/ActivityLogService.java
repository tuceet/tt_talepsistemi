package com.monad.talep.service;

import com.monad.talep.entity.ActivityLog;
import com.monad.talep.entity.AppUser;
import com.monad.talep.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository logRepo;

    public ActivityLogService(ActivityLogRepository logRepo) { this.logRepo = logRepo; }

    public void log(AppUser user, String actionType, String entityType, Long entityId, String detail) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetail(detail);
        logRepo.save(log);
    }

    public List<ActivityLog> latest() { return logRepo.findTop50ByOrderByCreatedAtDesc(); }

    public List<ActivityLog> latestOf(AppUser user) { return logRepo.findTop50ByUserOrderByCreatedAtDesc(user); }
}
