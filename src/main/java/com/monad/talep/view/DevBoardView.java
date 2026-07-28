package com.monad.talep.view;

import com.monad.talep.entity.*;
import com.monad.talep.service.ActivityLogService;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.PrioritizationService;
import com.monad.talep.service.RequestService;
import com.monad.talep.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** EKRAN 3: Yazilimci -- zengin "Gorevlerim" paneli: istatistik kartlari, filtreli tablo,
 *  aktivite akisi, durum dagilim donutu ve tamamlama grafigi. */
@Route(value = "dev", layout = MainLayout.class)
@AnonymousAllowed
public class DevBoardView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DDF = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Grid<TaskItem> grid = new Grid<>();
    private List<TaskItem> allTasks;
    private final TextField search = new TextField();
    private final Select<TaskStatus> statusFilter = new Select<>();
    private final Select<String> priorityFilter = new Select<>();
    private PrioritizationService prioritizationService;

    public DevBoardView(AuthService authService, TaskService taskService, RequestService requestService,
                        PrioritizationService prioritizationService, ActivityLogService logService) {
        AppUser me = authService.currentUser();
        if (me == null) return;
        if (me.getRole().getRoleName() != RoleName.DEVELOPER) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }
        this.prioritizationService = prioritizationService;

        add(new H3("Görevlerim"));

        allTasks = taskService.myTasks(me);

        // ---- Istatistik kartlari ----
        long totalCount = allTasks.size();
        long inProgress = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long testing = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TESTING).count();
        long done = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long overdue = allTasks.stream().filter(t -> t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.DONE).count();

        HorizontalLayout stats = new HorizontalLayout(
                statCard("Toplam Görev", String.valueOf(totalCount), "#1F4B8F"),
                statCard("Devam Eden", String.valueOf(inProgress), "#D98E2B"),
                statCard("Test Aşamasında", String.valueOf(testing), "#6941C6"),
                statCard("Tamamlanan", String.valueOf(done), "#2E9469"),
                statCard("Geciken", String.valueOf(overdue), "#C0433F"));
        stats.setWidthFull();
        add(stats);

        // ---- Filtreler ----
        search.setPlaceholder("Görev veya talep ara...");
        search.setPrefixComponent(com.vaadin.flow.component.icon.VaadinIcon.SEARCH.create());
        search.setWidth("260px");
        search.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refreshGrid());

        statusFilter.setItems(TaskStatus.values());
        statusFilter.setEmptySelectionAllowed(true);
        statusFilter.setEmptySelectionCaption("Durum: Tümü");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        priorityFilter.setItems("Yüksek", "Orta", "Düşük");
        priorityFilter.setEmptySelectionAllowed(true);
        priorityFilter.setEmptySelectionCaption("Öncelik: Tümü");
        priorityFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout filters = new HorizontalLayout(search, statusFilter, priorityFilter);
        filters.setAlignItems(FlexComponent.Alignment.END);
        add(filters);

        // ---- Ana govde: sol tablo + sag aktivite akisi ----
        HorizontalLayout body = new HorizontalLayout();
        body.setWidthFull();
        body.getStyle().set("align-items", "flex-start").set("gap", "1.2rem").set("flex-wrap", "wrap");

        VerticalLayout main = new VerticalLayout();
        main.setPadding(false);
        main.getStyle().set("flex", "3 1 640px").set("min-width", "0");

        grid.addColumn(TaskItem::getId).setHeader("#").setWidth("60px").setFlexGrow(0);
        grid.addColumn(TaskItem::getTaskTitle).setHeader("Görev");
        grid.addColumn(t -> "#" + t.getRequest().getId() + " " + t.getRequest().getTitle()).setHeader("Talep");
        grid.addComponentColumn(t -> priorityChip(prioritizationService.of(t.getRequest()).map(Prioritization::getUrgency).orElse(0)))
                .setHeader("Öncelik").setWidth("100px").setFlexGrow(0);
        grid.addComponentColumn(t -> {
            Select<TaskStatus> s = new Select<>();
            s.setItems(TaskStatus.values());
            s.setValue(t.getStatus());
            s.addValueChangeListener(e -> {
                taskService.updateStatus(t, e.getValue(), me);
                Notification.show("Durum: " + e.getValue());
                refreshStatsAndGrid(taskService, me);
            });
            return s;
        }).setHeader("Durum").setWidth("170px").setFlexGrow(0);
        grid.addComponentColumn(t -> {
            ProgressBar pb = new ProgressBar();
            pb.setMin(0); pb.setMax(100);
            pb.setValue(progressPercent(t.getStatus()));
            pb.setWidth("90px");
            return pb;
        }).setHeader("İlerleme").setWidth("110px").setFlexGrow(0);
        grid.addColumn(t -> t.getDueDate() != null ? t.getDueDate().format(DDF) : "-").setHeader("Teslim Tarihi").setWidth("120px").setFlexGrow(0);
        grid.addComponentColumn(t -> remainingChip(t.getDueDate(), t.getStatus())).setHeader("Kalan").setWidth("100px").setFlexGrow(0);
        grid.addColumn(t -> requestService.comments(t.getRequest()).size()).setHeader("Yorum").setWidth("80px").setFlexGrow(0);
        grid.addComponentColumn(t -> {
            Button detail = new Button("Detay", e -> openDetailDialog(t.getRequest(), requestService, me));
            detail.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            return detail;
        }).setHeader("").setWidth("90px").setFlexGrow(0);
        grid.setWidthFull();
        grid.setItems(allTasks);
        main.add(grid);

        // ---- Alt: donut + tamamlama grafigi ----
        HorizontalLayout charts = new HorizontalLayout();
        charts.getStyle().set("flex-wrap", "wrap").set("gap", "1rem").set("margin-top", "1rem").set("width", "100%");
        charts.add(chartPanel("Görev Dağılımı", donutChart(allTasks)));
        charts.add(chartPanel("Tamamlama Grafiği (Son 7 Gün)", completionLineChart(allTasks)));
        charts.add(chartPanel("Ortalama Tamamlama Süresi", avgCompletionBox(allTasks)));
        main.add(charts);

        // ---- Sag: aktivite akisi ----
        VerticalLayout side = new VerticalLayout();
        side.setPadding(false);
        side.getStyle().set("flex", "1 1 260px").set("min-width", "240px");
        side.add(activityPanel(logService, me));

        body.add(main, side);
        add(body);
    }

    private void refreshStatsAndGrid(TaskService taskService, AppUser me) {
        allTasks = taskService.myTasks(me);
        refreshGrid();
    }

    private void refreshGrid() {
        String q = search.getValue() == null ? "" : search.getValue().toLowerCase();
        TaskStatus st = statusFilter.getValue();
        String pr = priorityFilter.getValue();
        List<TaskItem> filtered = allTasks.stream()
                .filter(t -> q.isBlank() || t.getTaskTitle().toLowerCase().contains(q)
                        || t.getRequest().getTitle().toLowerCase().contains(q))
                .filter(t -> st == null || t.getStatus() == st)
                .filter(t -> pr == null || pr.equals(priorityLabel(
                        prioritizationService.of(t.getRequest()).map(Prioritization::getUrgency).orElse(0))))
                .toList();
        grid.setItems(filtered);
    }

    private String priorityLabel(int urgency) {
        if (urgency >= 4) return "Yüksek";
        if (urgency == 3) return "Orta";
        if (urgency >= 1) return "Düşük";
        return "-";
    }

    private int progressPercent(TaskStatus status) {
        return switch (status) {
            case BACKLOG -> 0;
            case ASSIGNED -> 20;
            case IN_PROGRESS -> 60;
            case TESTING -> 85;
            case DONE -> 100;
        };
    }

    private Span priorityChip(int urgency) {
        String label; String color;
        if (urgency == 0) { label = "-"; color = "var(--lumo-secondary-text-color)"; }
        else if (urgency >= 4) { label = "Yüksek"; color = "#C0433F"; }
        else if (urgency == 3) { label = "Orta"; color = "#D98E2B"; }
        else { label = "Düşük"; color = "#2E9469"; }
        Span s = new Span("● " + label);
        s.getStyle().set("color", color).set("font-size", "0.85rem").set("font-weight", "600");
        return s;
    }

    private Span remainingChip(LocalDate dueDate, TaskStatus status) {
        if (dueDate == null) return styledSpan("-", "var(--lumo-secondary-text-color)");
        if (status == TaskStatus.DONE) return styledSpan("Tamamlandı", "#2E9469");
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days < 0) return styledSpan("Geçti", "#C0433F");
        if (days == 0) return styledSpan("Bugün", "#D98E2B");
        return styledSpan(days + " gün", "var(--lumo-body-text-color)");
    }

    private Span styledSpan(String text, String color) {
        Span s = new Span(text);
        s.getStyle().set("color", color).set("font-size", "0.85rem");
        return s;
    }

    private Div statCard(String label, String value, String color) {
        Div d = new Div();
        d.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-left", "6px solid " + color)
                .set("border-radius", "10px").set("padding", "0.9rem 1.3rem")
                .set("min-width", "140px").set("flex", "1");
        Span v = new Span(value);
        v.getStyle().set("font-size", "1.6rem").set("font-weight", "700").set("display", "block")
                .set("font-family", "'Space Grotesk', sans-serif");
        Span l = new Span(label);
        l.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.82rem");
        d.add(v, l);
        return d;
    }

    private Div chartPanel(String title, com.vaadin.flow.component.Component content) {
        Div p = new Div();
        p.addClassName("tt-card");
        p.getStyle().set("flex", "1 1 260px").set("min-width", "220px");
        H4 h = new H4(title);
        h.getStyle().set("margin", "0 0 0.8rem 0").set("font-size", "0.95rem");
        p.add(h, content);
        return p;
    }

    private VerticalLayout donutChart(List<TaskItem> tasks) {
        TaskStatus[] statuses = TaskStatus.values();
        long[] values = new long[statuses.length];
        for (TaskItem t : tasks) values[t.getStatus().ordinal()]++;
        String[] colors = {"#8790A3", "#0891b2", "#D98E2B", "#6941C6", "#2E9469"};
        long total = tasks.size();

        Div donut = new Div();
        donut.getStyle().set("width", "120px").set("height", "120px").set("border-radius", "50%")
                .set("position", "relative").set("margin", "0 auto 0.8rem");
        if (total == 0) {
            donut.getStyle().set("background", "var(--lumo-contrast-10pct)");
        } else {
            StringBuilder g = new StringBuilder("conic-gradient(");
            double acc = 0;
            for (int i = 0; i < values.length; i++) {
                double start = acc / total * 360; acc += values[i];
                double end = acc / total * 360;
                g.append(colors[i]).append(" ").append(start).append("deg ").append(end).append("deg");
                if (i < values.length - 1) g.append(", ");
            }
            g.append(")");
            donut.getStyle().set("background", g.toString());
        }
        Div hole = new Div();
        hole.getStyle().set("position", "absolute").set("top", "16px").set("left", "16px")
                .set("width", "88px").set("height", "88px").set("border-radius", "50%")
                .set("background", "var(--lumo-base-color)").set("display", "flex")
                .set("flex-direction", "column").set("align-items", "center").set("justify-content", "center");
        Span tot = new Span(String.valueOf(total));
        tot.getStyle().set("font-weight", "700").set("font-size", "1.2rem").set("font-family", "'Space Grotesk', sans-serif");
        hole.add(tot);
        donut.add(hole);

        VerticalLayout legend = new VerticalLayout();
        legend.setPadding(false); legend.setSpacing(false);
        for (int i = 0; i < statuses.length; i++) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.getStyle().set("gap", "0.4rem").set("margin-bottom", "0.2rem");
            Div dot = new Div();
            dot.getStyle().set("width", "8px").set("height", "8px").set("border-radius", "50%").set("background", colors[i]);
            Span text = new Span(statuses[i].name() + ": " + values[i]);
            text.getStyle().set("font-size", "0.78rem");
            row.add(dot, text);
            legend.add(row);
        }
        VerticalLayout wrap = new VerticalLayout(donut, legend);
        wrap.setPadding(false);
        wrap.setAlignItems(FlexComponent.Alignment.CENTER);
        return wrap;
    }

    private com.vaadin.flow.component.Html completionLineChart(List<TaskItem> tasks) {
        int days = 7;
        int[] counts = new int[days];
        LocalDate today = LocalDate.now();
        for (TaskItem t : tasks) {
            if (t.getStatus() != TaskStatus.DONE) continue;
            long diff = java.time.temporal.ChronoUnit.DAYS.between(t.getUpdatedAt().toLocalDate(), today);
            if (diff >= 0 && diff < days) counts[(int) (days - 1 - diff)]++;
        }
        int max = 1;
        for (int c : counts) max = Math.max(max, c);

        StringBuilder points = new StringBuilder();
        StringBuilder labels = new StringBuilder("<div style='display:flex;justify-content:space-between;font-size:0.62rem;color:var(--lumo-secondary-text-color);margin-top:4px;'>");
        for (int i = 0; i < days; i++) {
            double x = (i * 200.0) / (days - 1);
            double y = 60 - (counts[i] * 50.0 / max);
            points.append(x).append(",").append(y);
            if (i < days - 1) points.append(" ");
            labels.append("<span>").append(today.minusDays(days - 1 - i).format(DateTimeFormatter.ofPattern("dd.MM"))).append("</span>");
        }
        labels.append("</div>");

        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='100%' height='70' viewBox='0 0 200 70'>"
                + "<polyline fill='none' stroke='#2E9469' stroke-width='2' points='" + points + "' /></svg>";
        return new com.vaadin.flow.component.Html("<div>" + svg + labels + "</div>");
    }

    private Div avgCompletionBox(List<TaskItem> tasks) {
        Div d = new Div();
        List<TaskItem> done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).toList();
        String value = "-";
        if (!done.isEmpty()) {
            double avgHours = done.stream()
                    .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCreatedAt()).toHours())
                    .average().orElse(0);
            value = String.format("%.1f gün", avgHours / 24.0);
        }
        Span v = new Span(value);
        v.getStyle().set("font-size", "1.8rem").set("font-weight", "700")
                .set("font-family", "'Space Grotesk', sans-serif").set("display", "block");
        Span l = new Span("tamamlanan " + done.size() + " görev üzerinden");
        l.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.78rem");
        d.add(v, l);
        return d;
    }

    private Div activityPanel(ActivityLogService logService, AppUser me) {
        Div panel = new Div();
        panel.addClassName("tt-card");
        H4 h = new H4("Aktivite Akışı");
        h.getStyle().set("margin", "0 0 0.8rem 0").set("font-size", "0.95rem");
        panel.add(h);
        List<ActivityLog> logs = logService.latestOf(me);
        if (logs.isEmpty()) {
            Span empty = new Span("Henüz aktivite yok");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");
            panel.add(empty);
        }
        int shown = 0;
        for (ActivityLog log : logs) {
            if (shown++ >= 8) break;
            Div item = new Div();
            item.getStyle().set("margin-bottom", "0.7rem").set("padding-bottom", "0.7rem")
                    .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
            Span action = new Span(log.getDetail());
            action.getStyle().set("display", "block").set("font-size", "0.85rem");
            Span time = new Span(log.getCreatedAt().format(DF));
            time.getStyle().set("display", "block").set("font-size", "0.7rem")
                    .set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0.15rem");
            item.add(action, time);
            panel.add(item);
        }
        return panel;
    }

    /** Müşterinin açtığı talebin tam detayı + yorum akışı (yazılımcı buradan cevap yazabilir). */
    private void openDetailDialog(Request r, RequestService requestService, AppUser me) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Talep #" + r.getId() + " — " + r.getTitle());
        dialog.setWidth("560px");
        dialog.setMaxHeight("80vh");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span meta = new Span("Müşteri: " + r.getCustomer().getNameSurname()
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
}