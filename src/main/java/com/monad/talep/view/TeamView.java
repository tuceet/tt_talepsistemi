package com.monad.talep.view;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.entity.TaskItem;
import com.monad.talep.entity.TaskStatus;
import com.monad.talep.entity.TeamMessage;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.TaskService;
import com.monad.talep.service.TeamMessageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** EKRAN: Yazilimci ekibi icin ortak alan -- canli calisma durumu + basit takim sohbeti. */
@Route(value = "takim", layout = MainLayout.class)
@AnonymousAllowed
public class TeamView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final VerticalLayout messageList = new VerticalLayout();

    public TeamView(AuthService authService, AppUserRepository userRepo, TaskService taskService,
                    TeamMessageService messageService) {
        AppUser me = authService.currentUser();
        if (me == null) return;
        RoleName role = me.getRole().getRoleName();
        if (role != RoleName.DEVELOPER && role != RoleName.PRODUCT_OWNER) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }

        add(new H3("Takım"));

        // ---- Canli calisma durumu ----
        add(new H4("Canlı Çalışma Durumu"));
        Grid<AppUser> statusGrid = new Grid<>();
        List<AppUser> devs = userRepo.findByRole_RoleName(RoleName.DEVELOPER);
        statusGrid.addColumn(AppUser::getNameSurname).setHeader("Yazılımcı");
        statusGrid.addComponentColumn(dev -> {
            TaskItem current = taskService.myTasks(dev).stream()
                    .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                    .findFirst().orElse(null);
            if (current == null) {
                Span s = new Span("● Boşta");
                s.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9rem");
                return s;
            }
            Span s = new Span("● " + current.getTaskTitle());
            s.getStyle().set("color", "#D98E2B").set("font-weight", "600").set("font-size", "0.9rem");
            return s;
        }).setHeader("Şu An Üzerinde");
        statusGrid.addComponentColumn(dev -> {
            long testing = taskService.myTasks(dev).stream().filter(t -> t.getStatus() == TaskStatus.TESTING).count();
            long done = taskService.myTasks(dev).stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            Span s = new Span(testing + " test aşamasında · " + done + " tamamlandı");
            s.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.82rem");
            return s;
        }).setHeader("Özet");
        statusGrid.setItems(devs);
        statusGrid.setWidthFull();
        statusGrid.setAllRowsVisible(true);
        add(statusGrid);

        Button refreshStatus = new Button("Durumu Yenile", VaadinIcon.REFRESH.create(), e -> {
            statusGrid.getDataProvider().refreshAll();
            Notification.show("Güncellendi");
        });
        refreshStatus.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        add(refreshStatus);

        // ---- Takim sohbeti ----
        add(new H4("Takım Sohbeti"));
        Div chatCard = new Div();
        chatCard.addClassName("tt-card");
        chatCard.setWidthFull();
        chatCard.getStyle().set("box-sizing", "border-box").set("overflow", "hidden");

        messageList.setPadding(false);
        messageList.setSpacing(false);
        messageList.setWidthFull();
        messageList.getStyle().set("max-height", "320px").set("overflow-y", "auto").set("overflow-x", "hidden")
                .set("margin-bottom", "1rem").set("box-sizing", "border-box");
        loadMessages(messageService, me);
        chatCard.add(messageList);

        TextArea input = new TextArea();
        input.setPlaceholder("Takıma bir mesaj yaz...");
        input.setWidthFull();
        input.setHeight("70px");

        Button send = new Button("Gönder", VaadinIcon.PAPERPLANE.create(), e -> {
            if (input.isEmpty()) {
                Notification.show("Mesaj boş olamaz");
                return;
            }
            messageService.send(me, input.getValue());
            input.clear();
            loadMessages(messageService, me);
        });
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout inputRow = new HorizontalLayout(input, send);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.expand(input);
        chatCard.add(inputRow);

        add(chatCard);
    }

    private void loadMessages(TeamMessageService messageService, AppUser me) {
        messageList.removeAll();
        List<TeamMessage> messages = messageService.latest();
        if (messages.isEmpty()) {
            Span empty = new Span("Henüz mesaj yok. İlk mesajı sen yaz!");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9rem");
            messageList.add(empty);
            return;
        }
        for (TeamMessage m : messages) {
            boolean mine = m.getSender().getId().equals(me.getId());

            Div bubble = new Div();
            bubble.getStyle()
                    .set("background", mine ? "var(--tt-accent-soft)" : "var(--lumo-contrast-5pct)")
                    .set("border-radius", "10px").set("padding", "0.6rem 0.9rem")
                    .set("max-width", "70%").set("box-sizing", "border-box")
                    .set("word-break", "break-word").set("overflow-wrap", "anywhere");
            Span who = new Span(m.getSender().getNameSurname() + " · " + m.getCreatedAt().format(DF));
            who.getStyle().set("display", "block").set("font-size", "0.72rem")
                    .set("color", "var(--lumo-secondary-text-color)").set("margin-bottom", "0.2rem");
            Span text = new Span(m.getContent());
            text.getStyle().set("font-size", "0.9rem");
            bubble.add(who, text);

            Div row = new Div(bubble);
            row.getStyle()
                    .set("display", "flex")
                    .set("justify-content", mine ? "flex-end" : "flex-start")
                    .set("width", "100%").set("box-sizing", "border-box")
                    .set("margin-bottom", "0.5rem");
            messageList.add(row);
        }
    }
}