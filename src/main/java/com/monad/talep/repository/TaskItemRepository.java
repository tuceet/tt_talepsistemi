package com.monad.talep.repository;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.TaskItem;
import com.monad.talep.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {
    List<TaskItem> findByDeveloperOrderByCreatedAtDesc(AppUser developer);
    List<TaskItem> findByStatus(TaskStatus status);
    long countByStatus(TaskStatus status);
}
