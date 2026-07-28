package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.NotificationService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthService authService;

    public MainLayout(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        AppUser user = authService.currentUser();
        if (user == null) return;

        DrawerToggle toggle = new DrawerToggle();
        H2 title = new H2("TT Talep Destek");
        title.getStyle().set("font-size", "1.1rem").set("margin", "0");

        long unread = notificationService.unreadCount(user);
        Span who = new Span(user.getNameSurname() + " · " + roleLabel(user.getRole().getRoleName())
                + (unread > 0 ? "  🔔" + unread : ""));
        who.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9rem");

        Button logout = new Button("Çıkış", VaadinIcon.SIGN_OUT.create(), e -> {
            authService.logout();
            getUI().ifPresent(ui -> ui.getPage().setLocation("/"));
        });
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(toggle, title, who, logout);
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.expand(title);
        header.getStyle().set("padding", "0 1rem");
        header.addClassName("tt-topbar");
        addToNavbar(header);

        Div brand = new Div();
        brand.addClassName("tt-sidebar-brand");
        Icon brandIcon = VaadinIcon.CLIPBOARD_CHECK.create();
        brandIcon.setSize("18px");
        brand.add(brandIcon, new Span("TT Talep Destek"));

        SideNav nav = new SideNav();
        RoleName role = user.getRole().getRoleName();
        switch (role) {
            case CUSTOMER -> {
                nav.addItem(new SideNavItem("Taleplerim", CustomerView.class, VaadinIcon.ENVELOPE_OPEN_O.create()));
                nav.addItem(new SideNavItem("Destek", SupportView.class, VaadinIcon.PHONE.create()));
                nav.addItem(new SideNavItem("Bildirimler", NotificationsView.class, VaadinIcon.BELL.create()));
            }
            case PRODUCT_OWNER -> {
                nav.addItem(new SideNavItem("PO Paneli", PoView.class, VaadinIcon.SCALE.create()));
                nav.addItem(new SideNavItem("Görev Atama", TaskAssignView.class, VaadinIcon.EXCHANGE.create()));
                nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.CHART_3D.create()));
                nav.addItem(new SideNavItem("Bildirimler", NotificationsView.class, VaadinIcon.BELL.create()));
            }
            case DEVELOPER -> {
                nav.addItem(new SideNavItem("Görev Panosu", DevBoardView.class, VaadinIcon.TASKS.create()));
                nav.addItem(new SideNavItem("Takım", TeamView.class, VaadinIcon.GROUP.create()));
                nav.addItem(new SideNavItem("Takvim", CalendarView.class, VaadinIcon.CALENDAR.create()));
                nav.addItem(new SideNavItem("Bildirimler", NotificationsView.class, VaadinIcon.BELL.create()));
            }
            case ADMIN -> {
                nav.addItem(new SideNavItem("Kullanıcı Yönetimi", AdminUsersView.class, VaadinIcon.USERS.create()));
                nav.addItem(new SideNavItem("Log Kayıtları", LogsView.class, VaadinIcon.FILE_TEXT_O.create()));
                nav.addItem(new SideNavItem("Bildirimler", NotificationsView.class, VaadinIcon.BELL.create()));
            }
        }
        nav.addItem(new SideNavItem("Profilim", ProfileView.class, VaadinIcon.USER.create()));

        Div sidebar = new Div(brand, nav);
        sidebar.addClassName("tt-sidebar");
        addToDrawer(sidebar);
    }

    private String roleLabel(RoleName r) {
        return switch (r) {
            case CUSTOMER -> "Müşteri";
            case PRODUCT_OWNER -> "Ürün Sorumlusu";
            case DEVELOPER -> "Yazılımcı";
            case ADMIN -> "Admin";
        };
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authService.currentUser() == null) {
            event.forwardTo(LoginView.class);
        }
    }
}