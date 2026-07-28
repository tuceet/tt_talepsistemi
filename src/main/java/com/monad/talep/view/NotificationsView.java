package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.NotificationEntity;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.NotificationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "notifications", layout = MainLayout.class)
@AnonymousAllowed
public class NotificationsView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Grid<NotificationEntity> grid = new Grid<>();

    public NotificationsView(AuthService authService, NotificationService notificationService) {
        AppUser me = authService.currentUser();
        if (me == null) return;

        add(new H3("Bildirimler"));
        grid.addColumn(n -> n.getCreatedAt().format(DF)).setHeader("Tarih").setWidth("160px").setFlexGrow(0);
        grid.addColumn(NotificationEntity::getMessage).setHeader("Mesaj");
        grid.addColumn(n -> n.isRead() ? "Okundu" : "Yeni").setHeader("Durum").setWidth("100px").setFlexGrow(0);
        grid.addComponentColumn(n -> {
            Button b = new Button("Okundu işaretle", e -> {
                notificationService.markRead(n);
                grid.setItems(notificationService.forUser(me));
            });
            b.setEnabled(!n.isRead());
            return b;
        }).setHeader("");
        grid.setWidthFull();
        grid.setItems(notificationService.forUser(me));
        add(grid);
    }
}
