package com.monad.talep.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatus;
import com.monad.talep.entity.TaskItem;
import com.monad.talep.entity.TaskStatus;
import com.monad.talep.repository.TaskItemRepository;

@Service
public class TaskService {

    private final TaskItemRepository taskRepo;
    private final RequestService requestService;
    private final NotificationService notificationService;
    private final ActivityLogService logService;

    public TaskService(TaskItemRepository taskRepo,
                       RequestService requestService,
                       NotificationService notificationService,
                       ActivityLogService logService) {
        this.taskRepo = taskRepo;
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.logService = logService;
    }

    /** PO onayladigi talebi 'Gorev'e donusturur. 1:N -> ayni talepten birden fazla gorev acilabilir. */
    @Transactional
    public TaskItem convertToTask(Request request, String taskTitle, AppUser developer, LocalDate dueDate, AppUser po) {
        TaskItem t = new TaskItem();
        t.setRequest(request);
        t.setTaskTitle(taskTitle);
        t.setDeveloper(developer);
        t.setDueDate(dueDate);
        t.setStatus(developer == null ? TaskStatus.BACKLOG : TaskStatus.ASSIGNED);
        t = taskRepo.save(t);

        if (request.getStatus() != RequestStatus.CONVERTED) {
            requestService.changeStatus(request, RequestStatus.CONVERTED, po);
        }
        if (developer != null) {
            notificationService.notify(developer, "Size yeni gorev atandi: " + taskTitle);
        }
        logService.log(po, "CONVERT_TO_TASK", "TASK", t.getId(), taskTitle);
        return t;
    }
    

    @Transactional
    public void updateStatus(TaskItem task, TaskStatus newStatus, AppUser by) {
        task.setStatus(newStatus);
        taskRepo.save(task);
        logService.log(by, "TASK_STATUS", "TASK", task.getId(), newStatus.name());
        if (newStatus == TaskStatus.DONE) {
            notificationService.notify(task.getRequest().getCustomer(),
                    "Talebinizle (#" + task.getRequest().getId() + ") ilgili gorev tamamlandi.");
        }
    }

    @Transactional
    public void setDueDate(TaskItem task, LocalDate dueDate, AppUser by) {
        task.setDueDate(dueDate);
        taskRepo.save(task);
        logService.log(by, "TASK_DUE_DATE", "TASK", task.getId(),
                dueDate == null ? "tarih silindi" : dueDate.toString());
    }

    @Transactional
    public void assign(TaskItem task, AppUser developer, AppUser by) {
        task.setDeveloper(developer);
        task.setStatus(TaskStatus.ASSIGNED);
        taskRepo.save(task);
        notificationService.notify(developer, "Size gorev atandi: " + task.getTaskTitle());
        logService.log(by, "ASSIGN_TASK", "TASK", task.getId(), developer.getNameSurname());
    }

    public List<TaskItem> all() { return taskRepo.findAll(); }

    public List<TaskItem> myTasks(AppUser dev) { return taskRepo.findByDeveloperOrderByCreatedAtDesc(dev); }

    public long countByStatus(TaskStatus s) { return taskRepo.countByStatus(s); }
       public List<TaskItem> byStatus(TaskStatus s) { return taskRepo.findByStatus(s); }

}
