package com.monad.talep.view;

import com.monad.talep.entity.RoleName;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.MailService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Split-screen koyu giris ekrani: solda marka+ozellikler (ikonsuz), sagda floating giris karti. */
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView(AuthService authService, MailService mailService) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#0A0B14");

        Div wrap = new Div();
        wrap.getStyle()
                .set("width", "100%").set("min-height", "100vh").set("box-sizing", "border-box")
                .set("display", "flex").set("flex-wrap", "wrap").set("align-items", "center")
                .set("justify-content", "center").set("gap", "2.5rem").set("padding", "3rem 4vw");

        wrap.add(brandingPanel(), loginCard(authService, mailService));
        add(wrap);
    }

    private Div brandingPanel() {
        Div panel = new Div();
        panel.getStyle().set("flex", "1 1 420px").set("max-width", "480px").set("position", "relative");

        VerticalLayout v = new VerticalLayout();
        v.setPadding(false);
        v.setSpacing(false);

        Div logoRow = new Div();
        Span top = new Span("TT TALEP");
        top.getStyle().set("display", "block").set("color", "white").set("font-family", "'Space Grotesk', sans-serif")
                .set("font-weight", "700").set("font-size", "1.6rem").set("letter-spacing", "0.03em");
        Span sub = new Span("DESTEK SİSTEMİ");
        sub.getStyle().set("display", "block").set("color", "#A78BFA").set("font-size", "0.95rem")
                .set("font-weight", "600").set("letter-spacing", "0.05em");
        logoRow.add(top, sub);

        Div rule = new Div();
        rule.getStyle().set("width", "50px").set("height", "3px")
                .set("background", "linear-gradient(90deg, #8B5CF6, #EC4899)")
                .set("margin", "0.9rem 0 1.6rem").set("border-radius", "999px");

        Html title = new Html("<div style='font-family:\"Space Grotesk\",sans-serif;font-size:2.5rem;"
                + "font-weight:700;line-height:1.2;color:white;'>"
                + "<span style='display:block;'>Taleplerinizi yönetin,</span>"
                + "<span style='background:linear-gradient(135deg,#EC4899,#F97316);"
                + "-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent;"
                + "color:transparent;'>değer</span>"
                + "<span style='color:white;'> yaratın.</span></div>");

        Paragraph desc = new Paragraph("Müşteri taleplerini toplayın, önceliklendirin, göreve dönüştürün ve tek merkezden takip edin.");
        desc.getStyle().set("color", "#C7CEDD").set("font-size", "1rem").set("margin-top", "1rem").set("line-height", "1.6");

        Div grid = new Div();
        grid.getStyle().set("display", "grid").set("grid-template-columns", "1fr 1fr")
                .set("gap", "1rem").set("margin-top", "2rem");
        grid.add(
                miniFeature("#8B5CF6", "Akıllı Önceliklendirme", "AHP + TOPSIS ile talepleri doğru şekilde önceliklendirin."),
                miniFeature("#3B82F6", "Görev Yönetimi", "Talepleri görevlere dönüştürün ve süreci adım adım takip edin."),
                miniFeature("#F97316", "Raporlama", "Detaylı raporlar ve istatistiklerle performansı ölçün."),
                miniFeature("#10B981", "Bildirim Sistemi", "Önemli gelişmelerden anında haberdar olun.")
        );

        v.add(logoRow, rule, title, desc, grid);
        panel.add(v);
        return panel;
    }

    private Div miniFeature(String color, String title, String desc) {
        Div card = new Div();
        card.getStyle().set("background", "rgba(255,255,255,0.03)").set("border", "1px solid rgba(255,255,255,0.08)")
                .set("border-top", "2px solid " + color).set("border-radius", "12px").set("padding", "1rem");
        H4 t = new H4(title);
        t.getStyle().set("color", "white").set("font-size", "0.92rem").set("margin", "0 0 0.3rem");
        Span d = new Span(desc);
        d.getStyle().set("color", "#8C97B5").set("font-size", "0.78rem").set("line-height", "1.4");
        card.add(t, d);
        return card;
    }

    private Div loginCard(AuthService authService, MailService mailService) {
        Div card = new Div();
        card.getStyle()
                .set("flex", "1 1 380px").set("max-width", "420px")
                .set("background", "#12121F").set("border-radius", "18px")
                .set("border", "1px solid rgba(255,255,255,0.08)")
                .set("box-shadow", "0 30px 80px rgba(0,0,0,0.5)")
                .set("padding", "2.2rem 2rem").set("box-sizing", "border-box");

        H2 title = new H2("Hoş geldiniz");
        title.getStyle().set("color", "white").set("text-align", "center").set("margin", "0");
        Paragraph sub = new Paragraph("Hesabınıza giriş yaparak devam edin.");
        sub.getStyle().set("color", "#8C97B5").set("text-align", "center").set("margin", "0.4rem 0 1.4rem")
                .set("font-size", "0.92rem");

        Div rule = new Div();
        rule.getStyle().set("height", "3px").set("width", "70px").set("margin", "0 auto 1.6rem")
                .set("border-radius", "999px").set("background", "linear-gradient(90deg, #8B5CF6, #F97316)");

        EmailField email = new EmailField();
        email.setPlaceholder("po@demo.com");
        styleDarkField(email, "E-posta");

        PasswordField password = new PasswordField();
        password.setPlaceholder("••••••••");
        styleDarkField(password, "Şifre");

        Checkbox remember = new Checkbox("Beni hatırla");
        remember.getStyle().set("color", "#C7CEDD");
        Button forgot = new Button("Şifremi Unuttum", e -> openForgotDialog(authService, mailService));
        forgot.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        forgot.getStyle().set("color", "#A78BFA");
        HorizontalLayout rememberRow = new HorizontalLayout(remember, forgot);
        rememberRow.setWidthFull();
        rememberRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        rememberRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Button loginBtn = new Button("Giriş Yap", e ->
                authService.login(email.getValue(), password.getValue())
                        .ifPresentOrElse(user -> {
                            authService.storeInSession(user);
                            RoleName role = user.getRole().getRoleName();
                            String target = switch (role) {
                                case CUSTOMER -> "taleplerim";
                                case PRODUCT_OWNER -> "po";
                                case DEVELOPER -> "dev";
                                case ADMIN -> "admin";
                            };
                            getUI().ifPresent(ui -> ui.navigate(target));
                        }, () -> Notification.show("E-posta veya şifre hatalı")
                                .addThemeVariants(NotificationVariant.LUMO_ERROR)));
        loginBtn.addClickShortcut(Key.ENTER);
        loginBtn.setWidthFull();
        loginBtn.getStyle()
                .set("background", "linear-gradient(135deg, #8B5CF6, #EC4899, #F97316)")
                .set("color", "white").set("font-weight", "600").set("border", "none")
                .set("border-radius", "10px").set("height", "46px").set("margin-top", "0.4rem");

        Hr hr = new Hr();
        hr.getStyle().set("border-color", "rgba(255,255,255,0.08)").set("margin", "1.4rem 0 1rem");
        Span veya = new Span("veya");
        veya.getStyle().set("color", "#8C97B5").set("font-size", "0.8rem").set("display", "block")
                .set("text-align", "center").set("margin-bottom", "1rem");

        Button register = new Button("Hesabın yok mu? Kayıt Ol", e -> getUI().ifPresent(ui -> ui.navigate("register")));
        register.setWidthFull();
        register.getStyle().set("color", "white").set("border", "1px solid rgba(255,255,255,0.15)")
                .set("border-radius", "10px").set("background", "transparent");

        Button home = new Button("← Ana sayfaya dön", e -> getUI().ifPresent(ui -> ui.navigate("")));
        home.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        home.getStyle().set("color", "#A78BFA").set("margin", "0.8rem auto 0").set("display", "block");

        VerticalLayout fields = new VerticalLayout(email, password, rememberRow, loginBtn);
        fields.setPadding(false);
        fields.setSpacing(true);

        card.add(title, sub, rule, fields, hr, veya, register, home);
        return card;
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

    /** Sifre sifirlama: gecici sifre uret, e-postaya gonder */
    private void openForgotDialog(AuthService authService, MailService mailService) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Şifre Sıfırlama");
        EmailField email = new EmailField("Kayıtlı e-posta adresiniz");
        email.setWidth("300px");

        Button send = new Button("Geçici Şifre Gönder", VaadinIcon.ENVELOPE.create(), e ->
                authService.resetPassword(email.getValue()).ifPresentOrElse(temp -> {
                    boolean mailed = mailService.sendPasswordReset(email.getValue(), temp);
                    if (mailed) {
                        Notification.show("Geçici şifre e-posta adresinize gönderildi")
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } else {
                        Notification.show("Mail ayarlanmadığı için ekranda gösteriliyor — geçici şifre: " + temp)
                                .setDuration(15000);
                    }
                    d.close();
                }, () -> Notification.show("Bu e-posta ile kayıtlı kullanıcı bulunamadı")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)));
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        d.add(new VerticalLayout(email));
        d.getFooter().add(new Button("İptal", e -> d.close()), send);
        d.open();
    }
}