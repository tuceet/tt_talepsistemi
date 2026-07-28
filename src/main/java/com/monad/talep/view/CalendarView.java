package com.monad.talep.view;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import com.monad.talep.entity.TaskItem;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Yazilimci: aylik gorev takvimi (termin tarihine gore) */
@Route(value = "takvim", layout = MainLayout.class)
@AnonymousAllowed
public class CalendarView extends VerticalLayout {

    private static final Locale TR = new Locale("tr", "TR");
    private YearMonth current = YearMonth.now();
    private final Div calendarHolder = new Div();
    private final H3 monthLabel = new H3();
    private final AppUser me;
    private final TaskService taskService;

    public CalendarView(AuthService authService, TaskService taskService) {
        this.taskService = taskService;
        this.me = authService.currentUser();
        if (me == null || me.getRole().getRoleName() != RoleName.DEVELOPER) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }

        Button prev = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> { current = current.minusMonths(1); render(); });
        Button next = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> { current = current.plusMonths(1); render(); });
        Button today = new Button("Bugün", e -> { current = YearMonth.now(); render(); });
        today.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout header = new HorizontalLayout(prev, monthLabel, next, today);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        add(header);

        calendarHolder.setWidthFull();
        add(calendarHolder);
        render();
    }

    private void render() {
        String monthName = current.getMonth().getDisplayName(TextStyle.FULL, TR);
        monthLabel.setText(monthName.substring(0, 1).toUpperCase(TR) + monthName.substring(1) + " " + current.getYear());
        calendarHolder.removeAll();

        List<TaskItem> tasks = taskService.myTasks(me);

        Div gridDiv = new Div();
        gridDiv.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, 1fr)")
                .set("gap", "6px")
                .set("width", "100%");

        String[] dayNames = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
        for (String dn : dayNames) {
            Span s = new Span(dn);
            s.getStyle().set("font-weight", "700").set("color", "#475569").set("text-align", "center");
            gridDiv.add(s);
        }

        LocalDate first = current.atDay(1);
        int lead = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue(); // 0..6
        for (int i = 0; i < lead; i++) gridDiv.add(new Div());

        for (int day = 1; day <= current.lengthOfMonth(); day++) {
            LocalDate date = current.atDay(day);
            Div cell = new Div();
            cell.getStyle()
                    .set("border", "1px solid #e5e7eb")
                    .set("border-radius", "8px")
                    .set("min-height", "86px")
                    .set("padding", "6px")
                    .set("background", date.equals(LocalDate.now()) ? "#eff6ff" : "white");
            Span num = new Span(String.valueOf(day));
            num.getStyle().set("font-weight", "600").set("display", "block")
                    .set("color", date.equals(LocalDate.now()) ? "#2563eb" : "#334155");
            cell.add(num);

            tasks.stream()
                    .filter(t -> date.equals(t.getDueDate()))
                    .forEach(t -> {
                        Span badge = new Span(t.getTaskTitle());
                        badge.getStyle()
                                .set("display", "block")
                                .set("font-size", "0.72rem")
                                .set("margin-top", "3px")
                                .set("padding", "2px 6px")
                                .set("border-radius", "6px")
                                .set("color", "white")
                                .set("background", statusColor(t))
                                .set("overflow", "hidden")
                                .set("text-overflow", "ellipsis")
                                .set("white-space", "nowrap");
                        badge.getElement().setProperty("title",
                                t.getTaskTitle() + " · " + t.getStatus().name());
                        cell.add(badge);
                    });
            gridDiv.add(cell);
        }
        calendarHolder.add(gridDiv);

        String undated = tasks.stream().filter(t -> t.getDueDate() == null)
                .map(TaskItem::getTaskTitle).collect(Collectors.joining(", "));
        if (!undated.isBlank()) {
            Span info = new Span("Terminsiz görevler: " + undated);
            info.getStyle().set("color", "#64748b").set("display", "block").set("margin-top", "1rem");
            calendarHolder.add(info);
        }
    }

    private String statusColor(TaskItem t) {
        return switch (t.getStatus()) {
            case BACKLOG -> "#94a3b8";
            case ASSIGNED -> "#3b82f6";
            case IN_PROGRESS -> "#d97706";
            case TESTING -> "#7c3aed";
            case DONE -> "#16a34a";
        };
    }
}
