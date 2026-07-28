package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Role;
import com.monad.talep.entity.RoleName;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.repository.RoleRepository;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.MailService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** ADMIN: uye ekleme + rol degistirme + aktif/pasif. Yazilimci eklemek SADECE buradan yapilir. */
@Route(value = "admin", layout = MainLayout.class)
@AnonymousAllowed
public class AdminUsersView extends VerticalLayout {

    private final Grid<AppUser> grid = new Grid<>();

    public AdminUsersView(AuthService authService, AppUserRepository userRepo,
                          RoleRepository roleRepo, MailService mailService) {
        AppUser me = authService.currentUser();
        if (me == null || me.getRole().getRoleName() != RoleName.ADMIN) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }

        add(new H3("Kullanıcı Yönetimi"));

        Button addUser = new Button("Yeni Üye Ekle", VaadinIcon.PLUS.create(),
                e -> openAddDialog(authService, userRepo, roleRepo, mailService));
        addUser.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(addUser);

        grid.addColumn(AppUser::getId).setHeader("#").setWidth("70px").setFlexGrow(0);
        grid.addColumn(AppUser::getNameSurname).setHeader("Ad Soyad");
        grid.addColumn(AppUser::getEmail).setHeader("E-posta");
        grid.addComponentColumn(u -> {
            Select<Role> roleSelect = new Select<>();
            roleSelect.setItems(roleRepo.findAll());
            roleSelect.setItemLabelGenerator(r -> r.getRoleName().name());
            roleSelect.setValue(u.getRole());
            roleSelect.addValueChangeListener(e -> {
                u.setRole(e.getValue());
                userRepo.save(u);
                Notification.show(u.getNameSurname() + " rolü: " + e.getValue().getRoleName());
            });
            return roleSelect;
        }).setHeader("Rol").setWidth("200px");
        grid.addComponentColumn((AppUser u) -> {
            com.vaadin.flow.component.Component cell;
            if (u.getRole().getRoleName() != RoleName.CUSTOMER) {
                cell = new com.vaadin.flow.component.html.Span("-");
            } else {
                Select<com.monad.talep.entity.PlanType> planSelect = new Select<>();
                planSelect.setItems(com.monad.talep.entity.PlanType.values());
                planSelect.setItemLabelGenerator(com.monad.talep.entity.PlanType::getDisplayName);
                planSelect.setValue(u.getPlan());
                planSelect.addValueChangeListener(e -> {
                    u.setPlan(e.getValue());
                    userRepo.save(u);
                    Notification.show(u.getNameSurname() + " planı: " + e.getValue().getDisplayName());
                });
                cell = planSelect;
            }
            return cell;
        }).setHeader("Plan (Müşteri)").setWidth("180px");
        grid.addComponentColumn(u -> {
            Button b = new Button(u.isActive() ? "Pasifleştir" : "Aktifleştir", e -> {
                u.setActive(!u.isActive());
                userRepo.save(u);
                refresh(userRepo);
            });
            return b;
        }).setHeader("Durum");
        grid.setWidthFull();
        add(grid);
        refresh(userRepo);
    }

    private void openAddDialog(AuthService authService, AppUserRepository userRepo,
                               RoleRepository roleRepo, MailService mailService) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Yeni Üye Ekle");

        TextField name = new TextField("Ad Soyad");
        EmailField email = new EmailField("E-posta");
        PasswordField pass = new PasswordField("Şifre");
        Select<Role> role = new Select<>();
        role.setLabel("Rol");
        role.setItems(roleRepo.findAll());
        role.setItemLabelGenerator(r -> r.getRoleName().name());

        Button save = new Button("Ekle", e -> {
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || role.getValue() == null) {
                Notification.show("Tüm alanları doldurun").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (userRepo.findByEmail(email.getValue()).isPresent()) {
                Notification.show("Bu e-posta zaten kayıtlı").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            AppUser u = new AppUser();
            u.setNameSurname(name.getValue());
            u.setEmail(email.getValue());
            u.setPasswordHash(authService.hash(pass.getValue()));
            u.setRole(role.getValue());
            userRepo.save(u);
            mailService.sendWelcome(u.getEmail(), u.getNameSurname());
            Notification.show("Üye eklendi: " + u.getNameSurname())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            d.close();
            refresh(userRepo);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        d.add(new VerticalLayout(name, email, pass, role));
        d.getFooter().add(new Button("İptal", e -> d.close()), save);
        d.open();
    }

    private void refresh(AppUserRepository userRepo) {
        grid.setItems(userRepo.findAll());
    }
}
