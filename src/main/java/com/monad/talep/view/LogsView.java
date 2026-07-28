package com.monad.talep.view;

import com.monad.talep.entity.ActivityLog;
import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.service.ActivityLogService;
import com.monad.talep.service.AuthService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

/** ADMIN: sistem log kayitlari (activity_log) */
@Route(value = "loglar", layout = MainLayout.class)
@AnonymousAllowed
public class LogsView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public LogsView(AuthService authService, ActivityLogService logService) {
        AppUser me = authService.currentUser();
        if (me == null || me.getRole().getRoleName() != RoleName.ADMIN) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }
        add(new H3("Sistem Log Kayıtları"));
        Grid<ActivityLog> grid = new Grid<>();
        grid.addColumn(l -> l.getCreatedAt().format(DF)).setHeader("Zaman").setWidth("170px").setFlexGrow(0);
        grid.addColumn(l -> l.getUser().getNameSurname()).setHeader("Kullanıcı");
        grid.addColumn(ActivityLog::getActionType).setHeader("Aksiyon");
        grid.addColumn(l -> l.getEntityType() + (l.getEntityId() != null ? " #" + l.getEntityId() : "")).setHeader("Nesne");
        grid.addColumn(ActivityLog::getDetail).setHeader("Detay");
        grid.setItems(logService.latest());
        grid.setWidthFull();
        add(grid);
    }
}
