package com.monad.talep.view;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.monad.talep.entity.ActivityLog;
import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RequestStatus;
import com.monad.talep.entity.RoleName;
import com.monad.talep.entity.TaskStatus;
import com.monad.talep.service.ActivityLogService;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.RequestService;
import com.monad.talep.service.TaskService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Rol duyarli dashboard:
 * - MUSTERI: sadece KENDI taleplerinin istatistigi ve KENDI aktiviteleri
 * - PO / DEVELOPER / ADMIN: sistem geneli istatistik + tum aktivite akisi
 */
@Route(value = "dashboard", layout = MainLayout.class)
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public DashboardView(AuthService authService, RequestService requestService,
                         TaskService taskService, ActivityLogService logService) {
        AppUser me = authService.currentUser();
        if (me == null) return;
        if (me.getRole().getRoleName() != RoleName.PRODUCT_OWNER) {
            add(new H3("Dashboard yalnızca Ürün Sorumlusu tarafından görüntülenebilir."));
            return;
        }
        boolean customer = false;

        add(new H3(customer ? "Taleplerim — Özet" : "Dashboard"));

        HorizontalLayout cards;
        if (customer) {
            cards = new HorizontalLayout(
                    card("Yeni", requestService.countMineByStatus(me, RequestStatus.NEW), "#2563eb"),
                    card("İncelemede", requestService.countMineByStatus(me, RequestStatus.UNDER_REVIEW), "#0891b2"),
                    card("Önceliklendirilen", requestService.countMineByStatus(me, RequestStatus.PRIORITIZED), "#7c3aed"),
                    card("Göreve Dönüşen", requestService.countMineByStatus(me, RequestStatus.CONVERTED), "#16a34a")
            );
        } else {
            cards = new HorizontalLayout(
                    card("Yeni Talep", requestService.countByStatus(RequestStatus.NEW), "#2563eb"),
                    card("Önceliklendirilen", requestService.countByStatus(RequestStatus.PRIORITIZED), "#7c3aed"),
                    card("Devam Eden Görev", taskService.countByStatus(TaskStatus.IN_PROGRESS), "#d97706"),
                    card("Tamamlanan", taskService.countByStatus(TaskStatus.DONE), "#16a34a")
            );
        }
        cards.setWidthFull();
        add(cards);

        add(new H3("Durum Dağılımı"));
        long[] values;
        String[] labels;
        String[] colors = {"#1F4B8F", "#6941C6", "#D98E2B", "#2E9469"};
        if (customer) {
            values = new long[]{
                    requestService.countMineByStatus(me, RequestStatus.NEW),
                    requestService.countMineByStatus(me, RequestStatus.UNDER_REVIEW),
                    requestService.countMineByStatus(me, RequestStatus.PRIORITIZED),
                    requestService.countMineByStatus(me, RequestStatus.CONVERTED)
            };
            labels = new String[]{"Yeni", "İncelemede", "Önceliklendirilen", "Göreve Dönüşen"};
        } else {
            values = new long[]{
                    requestService.countByStatus(RequestStatus.NEW),
                    requestService.countByStatus(RequestStatus.PRIORITIZED),
                    taskService.countByStatus(TaskStatus.IN_PROGRESS),
                    taskService.countByStatus(TaskStatus.DONE)
            };
            labels = new String[]{"Yeni Talep", "Önceliklendirilen", "Devam Eden Görev", "Tamamlanan"};
        }
        add(donutChart(values, labels, colors));

        if (!customer) {
            add(new H3("İş Akışı Panosu"));
            add(kanbanBoard(requestService, taskService));
        }

        add(new H3(customer ? "Son İşlemlerim" : "Son Aktiviteler"));
        Grid<ActivityLog> grid = new Grid<>();
        grid.addColumn(l -> l.getCreatedAt().format(DF)).setHeader("Zaman").setWidth("120px").setFlexGrow(0);
        if (!customer) grid.addColumn(l -> l.getUser().getNameSurname()).setHeader("Kullanıcı");
        grid.addColumn(ActivityLog::getActionType).setHeader("Aksiyon");
        grid.addColumn(ActivityLog::getDetail).setHeader("Detay");
        List<ActivityLog> logs = customer ? logService.latestOf(me) : logService.latest();
        grid.setItems(logs);
        grid.setWidthFull();
        add(grid);
    }

    /** Harici kutuphane olmadan, saf CSS conic-gradient ile donut grafik + lejant. */
    private HorizontalLayout donutChart(long[] values, String[] labels, String[] colors) {
        long total = 0;
        for (long v : values) total += v;

        Div donut = new Div();
        donut.getStyle()
                .set("width", "160px").set("height", "160px")
                .set("border-radius", "50%")
                .set("flex", "0 0 auto")
                .set("position", "relative");

        if (total == 0) {
            donut.getStyle().set("background", "var(--lumo-contrast-10pct)");
        } else {
            StringBuilder gradient = new StringBuilder("conic-gradient(");
            double acc = 0;
            for (int i = 0; i < values.length; i++) {
                double start = acc / total * 360;
                acc += values[i];
                double end = acc / total * 360;
                gradient.append(colors[i % colors.length])
                        .append(" ").append(start).append("deg ").append(end).append("deg");
                if (i < values.length - 1) gradient.append(", ");
            }
            gradient.append(")");
            donut.getStyle().set("background", gradient.toString());
        }

        Div hole = new Div();
        hole.getStyle()
                .set("position", "absolute").set("top", "20px").set("left", "20px")
                .set("width", "120px").set("height", "120px")
                .set("border-radius", "50%")
                .set("background", "var(--lumo-base-color)")
                .set("display", "flex").set("flex-direction", "column")
                .set("align-items", "center").set("justify-content", "center");
        Span totalNum = new Span(String.valueOf(total));
        totalNum.getStyle().set("font-family", "'Space Grotesk', sans-serif")
                .set("font-weight", "700").set("font-size", "1.6rem");
        Span totalLabel = new Span("toplam");
        totalLabel.getStyle().set("font-size", "0.75rem").set("color", "var(--lumo-secondary-text-color)");
        hole.add(totalNum, totalLabel);
        donut.add(hole);

        VerticalLayout legend = new VerticalLayout();
        legend.setPadding(false);
        legend.setSpacing(false);
        for (int i = 0; i < values.length; i++) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.getStyle().set("gap", "0.5rem").set("margin-bottom", "0.4rem");
            Div dot = new Div();
            dot.getStyle().set("width", "10px").set("height", "10px").set("border-radius", "50%")
                    .set("background", colors[i % colors.length]).set("flex", "0 0 auto");
            Span text = new Span(labels[i] + ": " + values[i]);
            text.getStyle().set("font-size", "0.92rem");
            row.add(dot, text);
            legend.add(row);
        }

        HorizontalLayout wrap = new HorizontalLayout(donut, legend);
        wrap.setAlignItems(FlexComponent.Alignment.CENTER);
        wrap.getStyle().set("gap", "2rem");
        wrap.addClassName("tt-card");
        wrap.setWidthFull();
        return wrap;
    }

    /** Jira tarzi, yatay kaydirmali is akisi panosu: Talep asamalari + Gorev asamalari. */
    private Div kanbanBoard(RequestService requestService, TaskService taskService) {
        Div board = new Div();
        board.getStyle()
                .set("display", "flex")
                .set("gap", "1rem")
                .set("overflow-x", "auto")
                .set("padding-bottom", "0.5rem")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        board.add(kanbanColumn("Yeni Talep", "#1F4B8F",
                requestService.byStatuses(List.of(RequestStatus.NEW)).stream()
                        .map(r -> new String[]{r.getTitle(), r.getCustomer().getNameSurname()})
                        .toList()));
        board.add(kanbanColumn("Önceliklendirilen", "#6941C6",
                requestService.byStatuses(List.of(RequestStatus.PRIORITIZED)).stream()
                        .map(r -> new String[]{r.getTitle(), r.getCustomer().getNameSurname()})
                        .toList()));
        board.add(kanbanColumn("Backlog", "#8790A3",
                taskService.byStatus(TaskStatus.BACKLOG).stream()
                        .map(t -> new String[]{t.getTaskTitle(), "Atanmadı"})
                        .toList()));
        board.add(kanbanColumn("Atandı", "#0891b2",
                taskService.byStatus(TaskStatus.ASSIGNED).stream()
                        .map(t -> new String[]{t.getTaskTitle(), t.getDeveloper() != null ? t.getDeveloper().getNameSurname() : "-"})
                        .toList()));
        board.add(kanbanColumn("Devam Ediyor", "#D98E2B",
                taskService.byStatus(TaskStatus.IN_PROGRESS).stream()
                        .map(t -> new String[]{t.getTaskTitle(), t.getDeveloper() != null ? t.getDeveloper().getNameSurname() : "-"})
                        .toList()));
        board.add(kanbanColumn("Test", "#C0433F",
                taskService.byStatus(TaskStatus.TESTING).stream()
                        .map(t -> new String[]{t.getTaskTitle(), t.getDeveloper() != null ? t.getDeveloper().getNameSurname() : "-"})
                        .toList()));
        board.add(kanbanColumn("Tamamlandı", "#2E9469",
                taskService.byStatus(TaskStatus.DONE).stream()
                        .map(t -> new String[]{t.getTaskTitle(), t.getDeveloper() != null ? t.getDeveloper().getNameSurname() : "-"})
                        .toList()));
        return board;
    }

    private VerticalLayout kanbanColumn(String title, String color, List<String[]> items) {
        VerticalLayout column = new VerticalLayout();
        column.setPadding(false);
        column.setSpacing(false);
        column.getStyle()
                .set("min-width", "220px").set("max-width", "220px")
                .set("flex", "0 0 auto")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("border-top", "4px solid " + color)
                .set("padding", "0.75rem");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        H4 t = new H4(title);
        t.getStyle().set("margin", "0").set("font-size", "0.95rem");
        Span count = new Span(String.valueOf(items.size()));
        count.getStyle().set("background", color).set("color", "white")
                .set("border-radius", "999px").set("padding", "0.05rem 0.55rem")
                .set("font-size", "0.75rem").set("font-weight", "700");
        header.add(t, count);
        header.expand(t);
        column.add(header);

        if (items.isEmpty()) {
            Span empty = new Span("Boş");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "0.85rem").set("margin-top", "0.5rem").set("display", "block");
            column.add(empty);
        }
        for (String[] item : items) {
            Div card = new Div();
            card.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("box-shadow", "0 1px 2px rgba(22,27,38,0.08)")
                    .set("padding", "0.6rem 0.75rem")
                    .set("margin-top", "0.6rem");
            Span cardTitle = new Span(item[0]);
            cardTitle.getStyle().set("display", "block").set("font-size", "0.86rem").set("font-weight", "600");
            Span cardMeta = new Span(item[1]);
            cardMeta.getStyle().set("display", "block").set("font-size", "0.76rem")
                    .set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0.2rem");
            card.add(cardTitle, cardMeta);
            column.add(card);
        }
        return column;
    }

    /** Ikonsuz ozet kart: sadece sayi + etiket, solda renk seridi. */
    private Div card(String label, long value, String color) {
        Div d = new Div();
        d.getStyle()
                .set("border", "1px solid #e5e7eb")
                .set("border-left", "6px solid " + color)
                .set("border-radius", "10px")
                .set("padding", "1.25rem 1.5rem")
                .set("min-width", "160px");
        Span v = new Span(String.valueOf(value));
        v.getStyle().set("font-size", "2rem").set("font-weight", "700").set("display", "block")
                .set("line-height", "1.1");
        Span l = new Span(label);
        l.getStyle().set("color", "#6b7280").set("font-size", "0.9rem");
        d.add(v, l);
        return d;
    }
}