package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.service.AuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** "Sorun mu yasiyorsunuz? Bizi arayin" destek ekrani. tel: linki tiklandiginda
 *  kullanicinin KENDI cihazindaki arama uygulamasi acilir (gercek otomatik arama
 *  sunucudan baslatilamaz, bunun icin Twilio gibi ucretli bir servis gerekir). */
@Route(value = "destek", layout = MainLayout.class)
@AnonymousAllowed
public class SupportView extends VerticalLayout {

    private static final String PHONE_DISPLAY = "0506 136 21 74";
    private static final String PHONE_TEL = "+905061362174";
    private static final String EMAIL = "destek@ttalep.com";

    public SupportView(AuthService authService) {
        AppUser me = authService.currentUser();
        if (me == null) return;

        setAlignItems(FlexComponent.Alignment.CENTER);

        Div card = new Div();
        card.addClassName("tt-card");
        card.getStyle().set("max-width", "480px").set("width", "100%").set("text-align", "center")
                .set("padding", "2.5rem 2rem");

        Icon phoneIcon = VaadinIcon.PHONE.create();
        phoneIcon.setSize("40px");
        phoneIcon.getStyle().set("color", "var(--tt-accent)");

        H2 title = new H2("Sorun mu yaşıyorsunuz?");
        title.getStyle().set("margin", "1rem 0 0.3rem");
        Paragraph sub = new Paragraph("Destek ekibimiz size yardımcı olmak için burada.");
        sub.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0 0 1.5rem");

        Anchor callLink = new Anchor("tel:" + PHONE_TEL, "");
        Button callBtn = new Button("Bizi Arayın: " + PHONE_DISPLAY, VaadinIcon.PHONE.create());
        callBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        callBtn.getStyle().set("width", "100%");
        callLink.add(callBtn);
        callLink.getStyle().set("display", "block").set("text-decoration", "none").set("width", "100%");
        callLink.getElement().setAttribute("router-ignore", true);

        Anchor mailLink = new Anchor("mailto:" + EMAIL, "");
        Button mailBtn = new Button("E-posta Gönder: " + EMAIL, VaadinIcon.ENVELOPE.create());
        mailBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        mailBtn.getStyle().set("width", "100%").set("margin-top", "0.7rem");
        mailLink.add(mailBtn);
        mailLink.getStyle().set("display", "block").set("text-decoration", "none").set("width", "100%");
        mailLink.getElement().setAttribute("router-ignore", true);

        HorizontalLayout hours = new HorizontalLayout();
        hours.setAlignItems(FlexComponent.Alignment.CENTER);
        hours.getStyle().set("justify-content", "center").set("gap", "0.4rem").set("margin-top", "1.3rem");
        Icon clock = VaadinIcon.CLOCK.create();
        clock.setSize("16px");
        clock.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span hoursText = new Span("Hafta içi 09:00 – 18:00 arası hizmetinizdeyiz");
        hoursText.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");
        hours.add(clock, hoursText);

        Button toRequests = new Button("Talep Oluşturmaya Dön", e -> getUI().ifPresent(ui -> ui.navigate("taleplerim")));
        toRequests.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        toRequests.getStyle().set("margin-top", "1.5rem");

        card.add(phoneIcon, title, sub, callLink, mailLink, hours, toRequests);
        add(card);
    }
}