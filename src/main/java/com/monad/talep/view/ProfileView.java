package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.service.AuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Kullanıcının kendi profilini (ad-soyad, e-posta, şifre) düzenlediği ekran. Tüm roller erişebilir. */
@Route(value = "profil", layout = MainLayout.class)
@AnonymousAllowed
public class ProfileView extends VerticalLayout {

    public ProfileView(AuthService authService, AppUserRepository userRepo) {
        AppUser me = authService.currentUser();
        if (me == null) return;

        setPadding(true);
        setMaxWidth("640px");

        add(new H2("Profilim"));

        // ---- Kimlik kartı ----
        Div avatar = new Div();
        avatar.setText(initials(me.getNameSurname()));
        avatar.getStyle()
                .set("width", "56px").set("height", "56px")
                .set("border-radius", "50%")
                .set("background", "var(--tt-ink)")
                .set("color", "#F3D9A8")
                .set("display", "flex").set("align-items", "center").set("justify-content", "center")
                .set("font-family", "'Space Grotesk', sans-serif").set("font-weight", "700")
                .set("font-size", "1.2rem");

        Span nameLine = new Span(me.getNameSurname());
        nameLine.getStyle().set("font-family", "'Space Grotesk', sans-serif").set("font-weight", "600")
                .set("font-size", "1.15rem").set("display", "block");
        Span roleLine = new Span(roleLabel(me.getRole().getRoleName())
                + (me.getRole().getRoleName() == RoleName.CUSTOMER ? " · " + me.getPlan().getDisplayName() + " plan" : ""));
        roleLine.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.92rem");

        VerticalLayout idText = new VerticalLayout(nameLine, roleLine);
        idText.setPadding(false);
        idText.setSpacing(false);

        HorizontalLayout idCard = new HorizontalLayout(avatar, idText);
        idCard.setAlignItems(FlexComponent.Alignment.CENTER);
        idCard.addClassName("tt-card");
        idCard.setWidthFull();
        add(idCard);

        // ---- Ad soyad / e-posta ----
        add(new H3("Bilgilerim"));
        TextField name = new TextField("Ad Soyad");
        name.setValue(me.getNameSurname());
        name.setWidthFull();
        EmailField email = new EmailField("E-posta");
        email.setValue(me.getEmail());
        email.setWidthFull();

        Button saveInfo = new Button("Bilgileri Kaydet", e -> {
            if (name.isEmpty() || email.isEmpty()) {
                Notification.show("Ad soyad ve e-posta zorunlu").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!email.getValue().equalsIgnoreCase(me.getEmail())
                    && userRepo.findByEmail(email.getValue()).isPresent()) {
                Notification.show("Bu e-posta başka bir hesapta kayıtlı").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            me.setNameSurname(name.getValue());
            me.setEmail(email.getValue());
            userRepo.save(me);
            authService.storeInSession(me);
            nameLine.setText(me.getNameSurname());
            Notification.show("Bilgileriniz güncellendi").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveInfo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div infoCard = new Div(name, email, saveInfo);
        infoCard.addClassName("tt-card");
        infoCard.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.9rem");
        infoCard.getStyle().set("margin-bottom", "1.25rem");
        add(infoCard);

        // ---- Şifre değiştirme ----
        add(new H3("Şifre Değiştir"));
        PasswordField current = new PasswordField("Mevcut Şifre");
        current.setWidthFull();
        PasswordField fresh = new PasswordField("Yeni Şifre");
        fresh.setWidthFull();
        PasswordField repeat = new PasswordField("Yeni Şifre (Tekrar)");
        repeat.setWidthFull();

        Button savePass = new Button("Şifreyi Güncelle", e -> {
            if (current.isEmpty() || fresh.isEmpty() || repeat.isEmpty()) {
                Notification.show("Tüm şifre alanlarını doldurun").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!authService.login(me.getEmail(), current.getValue()).isPresent()) {
                Notification.show("Mevcut şifre yanlış").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!fresh.getValue().equals(repeat.getValue())) {
                Notification.show("Yeni şifreler eşleşmiyor").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (fresh.getValue().length() < 4) {
                Notification.show("Yeni şifre en az 4 karakter olmalı").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            me.setPasswordHash(authService.hash(fresh.getValue()));
            userRepo.save(me);
            current.clear(); fresh.clear(); repeat.clear();
            Notification.show("Şifreniz güncellendi").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        savePass.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div passCard = new Div(current, fresh, repeat, savePass);
        passCard.addClassName("tt-card");
        passCard.addClassName("tt-card--muted");
        passCard.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.9rem");
        add(passCard);

        if (me.getRole().getRoleName() == RoleName.CUSTOMER) {
            Button toPlans = new Button("Plan Ayarlarına Git", e ->
                    getUI().ifPresent(ui -> ui.navigate(CustomerView.class)));
            toPlans.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            add(toPlans);
        }
    }

    private String initials(String nameSurname) {
        String[] parts = nameSurname.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.length() > 0 ? sb.toString() : "?";
    }

    private String roleLabel(RoleName r) {
        return switch (r) {
            case CUSTOMER -> "Müşteri";
            case PRODUCT_OWNER -> "Ürün Sorumlusu";
            case DEVELOPER -> "Yazılımcı";
            case ADMIN -> "Admin";
        };
    }
}
