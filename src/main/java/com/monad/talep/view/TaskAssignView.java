package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.entity.TaskItem;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.TaskService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

/**
 * PO: Gorev Atama ekrani.
 * PO yazilimci EKLEYEMEZ (bunu sadece Admin yapar);
 * yalnizca mevcut yazilimcilar arasinda atama/yeniden atama yapar.
 */
@Route(value = "atama", layout = MainLayout.class)
@AnonymousAllowed
public class TaskAssignView extends VerticalLayout {

    private final Grid<TaskItem> grid = new Grid<>();

    public TaskAssignView(AuthService authService, TaskService taskService, AppUserRepository userRepo) {
        AppUser me = authService.currentUser();
        if (me == null || me.getRole().getRoleName() != RoleName.PRODUCT_OWNER) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }

        add(new H3("Görev Atama"));
        Paragraph info = new Paragraph("Görevleri mevcut yazılımcılara atayın veya yeniden atayın. Yeni yazılımcı ekleme yetkisi yalnızca Admin'dedir.");
        info.getStyle().set("color", "#64748b");
        add(info);

        List<AppUser> developers = userRepo.findByRole_RoleName(RoleName.DEVELOPER);

        grid.addColumn(TaskItem::getId).setHeader("#").setWidth("70px").setFlexGrow(0);
        grid.addColumn(TaskItem::getTaskTitle).setHeader("Görev");
        grid.addColumn(t -> "#" + t.getRequest().getId() + " " + t.getRequest().getTitle()).setHeader("Talep");
        grid.addColumn(t -> t.getStatus().name()).setHeader("Durum").setWidth("130px");
        grid.addComponentColumn(t -> {
            ComboBox<AppUser> devBox = new ComboBox<>();
            devBox.setItems(developers);
            devBox.setItemLabelGenerator(AppUser::getNameSurname);
            devBox.setValue(t.getDeveloper());
            devBox.setPlaceholder("Yazılımcı seç");
            devBox.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    taskService.assign(t, e.getValue(), me);
                    Notification.show(t.getTaskTitle() + " → " + e.getValue().getNameSurname());
                    refresh(taskService);
                }
            });
            return devBox;
        }).setHeader("Atanan Yazılımcı").setWidth("220px");
        grid.addComponentColumn(t -> {
            DatePicker due = new DatePicker();
            due.setValue(t.getDueDate());
            due.setPlaceholder("Termin");
            due.addValueChangeListener(e -> {
                taskService.setDueDate(t, e.getValue(), me);
                Notification.show("Termin güncellendi");
            });
            return due;
        }).setHeader("Termin").setWidth("170px");
        grid.setWidthFull();
        add(grid);
        refresh(taskService);
    }

    private void refresh(TaskService taskService) {
        grid.setItems(taskService.all());
    }
}
