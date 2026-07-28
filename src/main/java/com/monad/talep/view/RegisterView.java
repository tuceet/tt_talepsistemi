package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.MailService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Koyu, Login/Landing ile ayni temada musteri kayit ekrani. */
@Route("register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(AuthService authService, MailService mailService) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "#0A0B14");

        Div glow = new Div();
        glow.getStyle()
                .set("position", "fixed").set("top", "-250px").set("left", "50%")
                .set("transform", "translateX(-50%)").set("width", "800px").set("height", "800px")
                .set("background", "radial-gradient(circle, rgba(139,92,246,0.28) 0%, rgba(236,72,153,0.12) 45%, transparent 70%)")
                .set("filter", "blur(50px)").set("z-index", "0").set("pointer-events", "none");
        add(glow);

        Div card = new Div();
        card.getStyle()
                .set("width", "420px").set("max-width", "92vw").set("position", "relative").set("z-index", "1")
                .set("background", "#12121F").set("border-radius", "18px")
                .set("border", "1px solid rgba(255,255,255,0.08)")
                .set("box-shadow", "0 30px 80px rgba(0,0,0,0.5)")
                .set("padding", "2.2rem 2rem").set("box-sizing", "border-box")
                .set("margin", "3rem 0");

        H2 title = new H2("Müşteri Kaydı");
        title.getStyle().set("color", "white").set("text-align", "center").set("margin", "0");
        Paragraph info = new Paragraph("Hesabınızı oluşturun, hemen talep açmaya başlayın.");
        info.getStyle().set("color", "#8C97B5").set("text-align", "center").set("margin", "0.4rem 0 1.4rem")
                .set("font-size", "0.92rem");

        Div rule = new Div();
        rule.getStyle().set("height", "3px").set("width", "70px").set("margin", "0 auto 1.6rem")
                .set("border-radius", "999px").set("background", "linear-gradient(90deg, #8B5CF6, #F97316)");

        TextField name = new TextField();
        name.setPlaceholder("Ad Soyad");
        name.setPrefixComponent(darkIcon(VaadinIcon.USER));
        styleDarkField(name, "Ad Soyad");

        EmailField email = new EmailField();
        email.setPlaceholder("ornek@eposta.com");
        email.setPrefixComponent(darkIcon(VaadinIcon.ENVELOPE));
        styleDarkField(email, "E-posta");

        PasswordField pass = new PasswordField();
        pass.setPlaceholder("••••••••");
        pass.setPrefixComponent(darkIcon(VaadinIcon.LOCK));
        styleDarkField(pass, "Şifre");

        PasswordField pass2 = new PasswordField();
        pass2.setPlaceholder("••••••••");
        pass2.setPrefixComponent(darkIcon(VaadinIcon.LOCK));
        styleDarkField(pass2, "Şifre (tekrar)");

        Button save = new Button("Kayıt Ol", e -> {
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Notification.show("Tüm alanları doldurun").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (!pass.getValue().equals(pass2.getValue())) {
                Notification.show("Şifreler eşleşmiyor").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            try {
                AppUser u = authService.registerCustomer(name.getValue(), email.getValue(), pass.getValue());
                mailService.sendWelcome(u.getEmail(), u.getNameSurname());
                Notification.show("Kayıt başarılı! Giriş yapabilirsiniz.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addClickShortcut(Key.ENTER);
        save.setWidthFull();
        save.getStyle()
                .set("background", "linear-gradient(135deg, #8B5CF6, #EC4899, #F97316)")
                .set("color", "white").set("font-weight", "600").set("border", "none")
                .set("border-radius", "10px").set("height", "46px").set("margin-top", "0.6rem");

        Button loginLink = new Button("Zaten hesabınız var mı? Giriş Yap", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        loginLink.getStyle().set("color", "#A78BFA").set("margin", "1rem auto 0").set("display", "block");

        Button home = new Button("← Ana sayfaya dön", e -> getUI().ifPresent(ui -> ui.navigate("")));
        home.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        home.getStyle().set("color", "#8C97B5").set("margin", "0.3rem auto 0").set("display", "block");

        VerticalLayout fields = new VerticalLayout(name, email, pass, pass2, save);
        fields.setPadding(false);
        fields.setSpacing(true);

        card.add(title, info, rule, fields, loginLink, home);
        add(card);
    }

    private Icon darkIcon(VaadinIcon type) {
        Icon icon = type.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "#8C97B5");
        return icon;
    }

    private void styleDarkField(TextFieldBase<?, ?> field, String label) {
        field.setLabel(label);
        field.setWidthFull();
        field.getStyle()
                .set("--lumo-contrast-10pct", "rgba(255,255,255,0.06)")
                .set("--lumo-body-text-color", "white")
                .set("--lumo-secondary-text-color", "#8C97B5")
                .set("--lumo-primary-color", "#8B5CF6");
    }
}