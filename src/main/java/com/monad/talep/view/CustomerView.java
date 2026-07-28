package com.monad.talep.view;

import java.time.format.DateTimeFormatter;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.CommentEntity;
import com.monad.talep.entity.PlanType;
import com.monad.talep.entity.Project;
import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatusHistory;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.repository.ProjectRepository;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.RequestService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** EKRAN 1: Müşteri — talep oluşturma + kendi taleplerini izleme */
@Route(value = "taleplerim", layout = MainLayout.class)
@AnonymousAllowed
public class CustomerView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Grid<Request> grid = new Grid<>();
    private final Span planBadge = new Span();
    private final Span planCountLabel = new Span();
    private final ProgressBar planProgress = new ProgressBar();
    private final HorizontalLayout upgradeButtons = new HorizontalLayout();

    public CustomerView(AuthService authService, RequestService requestService,
                        ProjectRepository projectRepo, AppUserRepository userRepo) {
        AppUser me = authService.currentUser();
        if (me == null) return;
        if (me.getRole().getRoleName() != com.monad.talep.entity.RoleName.CUSTOMER) {
            add(new H3("Bu sayfa yalnızca müşteriler içindir."));
            return;
        }

        add(new H3("Planım"));
        HorizontalLayout planRow = new HorizontalLayout(planBadge, planCountLabel);
        planRow.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        planProgress.setWidth("300px");
        VerticalLayout planCard = new VerticalLayout(planRow, planProgress, upgradeButtons);
        planCard.setPadding(false);
        planCard.setSpacing(true);
        planCard.addClassName("tt-card");
        planCard.setWidthFull();
        add(planCard);

        add(new H3("Yeni Talep Oluştur"));

        TextField title = new TextField("Başlık");
        title.setWidth("400px");
        ComboBox<Project> project = new ComboBox<>("Proje");
        project.setItems(projectRepo.findAll());
        project.setItemLabelGenerator(Project::getProjectName);
        TextArea desc = new TextArea("Açıklama");
        desc.setWidth("400px");

        Button save = new Button("Talebi Gönder", e -> {
            if (title.isEmpty() || desc.isEmpty()) {
                Notification.show("Başlık ve açıklama zorunlu");
                return;
            }
            try {
                requestService.createRequest(me, project.getValue(), title.getValue(), desc.getValue());
                title.clear(); desc.clear(); project.clear();
                refresh(requestService, me);
                refreshPlanSection(requestService, userRepo, me);
                Notification.show("Talep oluşturuldu");
            } catch (RequestService.PlanLimitExceededException ex) {
                Notification.show("Açık talep limitinize ulaştınız (" + me.getPlan().getDisplayName()
                        + " plan: " + me.getPlan().getMaxOpenRequests() + "). Devam etmek için planınızı yükseltin.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(title, project, desc, save, new H3("Taleplerim"));

        refreshPlanSection(requestService, userRepo, me);

        grid.addColumn(Request::getId).setHeader("#").setWidth("70px").setFlexGrow(0);
        grid.addColumn(Request::getTitle).setHeader("Başlık");
        grid.addColumn(r -> r.getProject() != null ? r.getProject().getProjectName() : "-").setHeader("Proje");
        grid.addColumn(r -> r.getStatus().name()).setHeader("Durum");
        grid.addColumn(r -> r.getCreatedAt().format(DF)).setHeader("Tarih");
        grid.addComponentColumn(r -> {
            Button detail = new Button("Detay", e -> openDetailDialog(r, requestService, me));
            detail.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            return detail;
        }).setHeader("").setWidth("100px").setFlexGrow(0);
        grid.setWidthFull();
        add(grid);

        refresh(requestService, me);
    }

    /** Talebin tam detayi: aciklama, durum gecmisi ve yorum akisi (yorum ekleme dahil). */
    private void openDetailDialog(Request r, RequestService requestService, AppUser me) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Talep #" + r.getId() + " — " + r.getTitle());
        dialog.setWidth("560px");
        dialog.setMaxHeight("80vh");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span meta = new Span((r.getProject() != null ? r.getProject().getProjectName() : "Proje yok")
                + " · " + r.getStatus().name() + " · " + r.getCreatedAt().format(DF));
        meta.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");
        Paragraph desc = new Paragraph(r.getDescription());
        content.add(meta, desc, new Hr());

        content.add(new H4("Durum Geçmişi"));
        for (RequestStatusHistory h : requestService.history(r)) {
            Span line = new Span((h.getOldStatus() != null ? h.getOldStatus().name() : "—")
                    + " → " + h.getNewStatus().name()
                    + "  ·  " + h.getChangedAt().format(DF)
                    + "  ·  " + h.getChangedBy().getNameSurname());
            line.getStyle().set("display", "block").set("font-size", "0.85rem")
                    .set("color", "var(--lumo-secondary-text-color)");
            content.add(line);
        }
        content.add(new Hr());

        content.add(new H4("Yorumlar"));
        VerticalLayout commentList = new VerticalLayout();
        commentList.setPadding(false);
        commentList.setSpacing(false);
        Runnable loadComments = () -> {
            commentList.removeAll();
            for (CommentEntity c : requestService.comments(r)) {
                Div item = new Div();
                item.addClassName("tt-card");
                item.addClassName("tt-card--muted");
                item.getStyle().set("margin-bottom", "0.5rem").set("padding", "0.75rem 1rem");
                Span who = new Span(c.getUser().getNameSurname() + " · " + c.getCreatedAt().format(DF));
                who.getStyle().set("display", "block").set("font-size", "0.78rem")
                        .set("color", "var(--lumo-secondary-text-color)").set("margin-bottom", "0.25rem");
                Span body = new Span(c.getContent());
                item.add(who, body);
                commentList.add(item);
            }
        };
        loadComments.run();
        content.add(commentList);

        TextArea newComment = new TextArea("Yorum ekle");
        newComment.setWidthFull();
        Button sendComment = new Button("Gönder", e -> {
            if (newComment.isEmpty()) {
                Notification.show("Yorum boş olamaz");
                return;
            }
            requestService.addComment(r, me, newComment.getValue());
            newComment.clear();
            loadComments.run();
        });
        sendComment.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(newComment, sendComment);

        dialog.add(content);
        dialog.getFooter().add(new Button("Kapat", e -> dialog.close()));
        dialog.open();
    }

    private void refresh(RequestService requestService, AppUser me) {
        grid.setItems(requestService.myRequests(me));
    }

    /** Plan rozeti, acik talep sayaci ve yukseltme butonlarini gunceller.
     * Odeme ekrani YOK: yukseltme/dusurme demo amacli anlik uygulanir. */
    private void refreshPlanSection(RequestService requestService, AppUserRepository userRepo, AppUser me) {
        PlanType plan = me.getPlan();
        long used = requestService.openRequestCount(me);
        int limit = plan.getMaxOpenRequests();

        planBadge.setText(plan.getDisplayName());
        planBadge.removeClassNames("tt-badge--free", "tt-badge--pro", "tt-badge--proplus");
        planBadge.addClassName("tt-badge");
        planBadge.addClassName(switch (plan) {
            case FREE -> "tt-badge--free";
            case PRO -> "tt-badge--pro";
            case PRO_PLUS -> "tt-badge--proplus";
        });
        planCountLabel.setText(plan.isUnlimited()
                ? ("Açık talepler: " + used + " / Sınırsız")
                : ("Açık talepler: " + used + " / " + limit));

        planProgress.setMin(0);
        if (plan.isUnlimited()) {
            planProgress.setMax(Math.max(1, used));
            planProgress.setValue(used);
        } else {
            planProgress.setMax(limit);
            planProgress.setValue(Math.min(used, limit));
        }

        upgradeButtons.removeAll();
        for (PlanType candidate : PlanType.values()) {
            if (candidate == plan) continue;
            Button b = new Button(candidate.getDisplayName() + "'e Geç", e -> {
                me.setPlan(candidate);
                userRepo.save(me);
                Notification.show("Planınız " + candidate.getDisplayName() + " olarak güncellendi")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshPlanSection(requestService, userRepo, me);
            });
            if (candidate.ordinal() > plan.ordinal()) {
                b.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }
            upgradeButtons.add(b);
        }
    }
}