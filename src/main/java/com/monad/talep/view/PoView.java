package com.monad.talep.view;

import java.util.ArrayList;
import java.util.List;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Prioritization;
import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatus;
import com.monad.talep.entity.RoleName;
import com.monad.talep.repository.AppUserRepository;
import com.monad.talep.service.AuthService;
import com.monad.talep.service.PrioritizationService;
import com.monad.talep.service.RequestService;
import com.monad.talep.service.TaskService;
import com.monad.talep.service.TopsisService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** EKRAN 2: Ürün Sorumlusu — listeleme, filtreleme, önceliklendirme (SIMPLE/AHP/TOPSIS), göreve dönüştürme */
@Route(value = "po", layout = MainLayout.class)
@AnonymousAllowed
public class PoView extends VerticalLayout {

    private final Grid<Request> grid = new Grid<>();
    private final RequestService requestService;
    private final PrioritizationService prioritizationService;
    private final TaskService taskService;
    private final TopsisService topsisService;
    private final AppUserRepository userRepo;
    private final AuthService authService;
    private RequestStatus filter = null;

    public PoView(AuthService authService, RequestService requestService,
                  PrioritizationService prioritizationService, TaskService taskService,
                  TopsisService topsisService, AppUserRepository userRepo) {
        this.authService = authService;
        this.requestService = requestService;
        this.prioritizationService = prioritizationService;
        this.taskService = taskService;
        this.topsisService = topsisService;
        this.userRepo = userRepo;

        AppUser me = authService.currentUser();
        if (me == null || me.getRole().getRoleName() != RoleName.PRODUCT_OWNER) {
            add(new H3("Bu sayfaya erişim yetkiniz yok."));
            return;
        }

        add(new H3("Ürün Sorumlusu Paneli"));

        Select<RequestStatus> statusFilter = new Select<>();
        statusFilter.setLabel("Duruma göre filtrele");
        statusFilter.setItems(RequestStatus.values());
        statusFilter.setEmptySelectionAllowed(true);
        statusFilter.setEmptySelectionCaption("Tümü");
        statusFilter.addValueChangeListener(e -> { filter = e.getValue(); refresh(); });

        Button topsisBtn = new Button("TOPSIS ile Toplu Sırala", e -> openTopsisDialog());
        topsisBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(statusFilter, topsisBtn);
        toolbar.setAlignItems(HorizontalLayout.Alignment.END);
        add(toolbar);

        grid.addColumn(Request::getId).setHeader("#").setWidth("70px").setFlexGrow(0);
        grid.addColumn(Request::getTitle).setHeader("Başlık");
        grid.addColumn(r -> r.getCustomer().getNameSurname()).setHeader("Müşteri");
        grid.addColumn(r -> r.getStatus().name()).setHeader("Durum");
        grid.addColumn(r -> prioritizationService.of(r)
                        .map(p -> p.getMethod() + ": " + p.getPriorityScore())
                        .orElse("-"))
                .setHeader("Öncelik");
        grid.addComponentColumn(this::actions).setHeader("İşlem").setWidth("300px");
        grid.setWidthFull();
        add(grid);
        refresh();
    }

    private HorizontalLayout actions(Request r) {
        Button review = new Button("İncele", e -> {
            requestService.changeStatus(r, RequestStatus.UNDER_REVIEW, authService.currentUser());
            refresh();
        });
        Button prioritize = new Button("Önceliklendir", e -> openPrioritizeDialog(r));
        prioritize.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button toTask = new Button("Göreve Dönüştür", e -> openTaskDialog(r));
        Button reject = new Button("Reddet", e -> {
            requestService.changeStatus(r, RequestStatus.REJECTED, authService.currentUser());
            refresh();
        });
        reject.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        HorizontalLayout hl = new HorizontalLayout();
        switch (r.getStatus()) {
            case NEW -> hl.add(review, reject);
            case UNDER_REVIEW -> hl.add(prioritize, reject);
            case PRIORITIZED, CONVERTED -> hl.add(toTask);
            default -> {}
        }
        return hl;
    }

    /** TOPSIS: birden fazla talebi ayni anda kiyaslayip ideal cozume yakinliga gore siralar. */
    private void openTopsisDialog() {
        List<Request> candidates = requestService.byStatuses(List.of(RequestStatus.UNDER_REVIEW));
        Dialog d = new Dialog();
        d.setHeaderTitle("TOPSIS ile Toplu Sıralama");
        d.setWidth("640px");
        d.setMaxHeight("80vh");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        if (candidates.isEmpty()) {
            content.add(new Paragraph("İncelemede (UNDER_REVIEW) durumunda talep yok. "
                    + "TOPSIS için önce talepleri incelemeye alın."));
            d.add(content);
            d.getFooter().add(new Button("Kapat", e -> d.close()));
            d.open();
            return;
        }

        content.add(new Paragraph("Her talep için Aciliyet/Etki (1-5) girin. AHP oranı kriterlerin göreli "
                + "önemini (ağırlığını) belirler; TOPSIS bu ağırlıklarla tüm talepleri birlikte sıralar."));

        NumberField ahpCmp = new NumberField("AHP: Etki / Aciliyet önem oranı (Saaty 1-9)");
        ahpCmp.setMin(0.12); ahpCmp.setMax(9); ahpCmp.setValue(3.0); ahpCmp.setStep(0.5);
        content.add(ahpCmp);

        record Row(Request request, IntegerField urgency, IntegerField impact) {}
        List<Row> rows = new ArrayList<>();
        for (Request r : candidates) {
            IntegerField urgency = new IntegerField("Aciliyet");
            urgency.setMin(1); urgency.setMax(5); urgency.setValue(3); urgency.setWidth("100px");
            IntegerField impact = new IntegerField("Etki");
            impact.setMin(1); impact.setMax(5); impact.setValue(3); impact.setWidth("100px");
            Span title = new Span("#" + r.getId() + " " + r.getTitle() + " (" + r.getCustomer().getNameSurname() + ")");
            title.getStyle().set("min-width", "260px").set("display", "inline-block");
            HorizontalLayout row = new HorizontalLayout(title, urgency, impact);
            row.setAlignItems(HorizontalLayout.Alignment.END);
            content.add(row);
            rows.add(new Row(r, urgency, impact));
        }

        VerticalLayout resultArea = new VerticalLayout();
        resultArea.setPadding(false);
        content.add(resultArea);

        Button rank = new Button("Sırala (TOPSIS)", e -> {
            double c = ahpCmp.getValue() == null ? 3.0 : ahpCmp.getValue();
            double wImpact = c / (c + 1);
            double wUrgency = 1 / (c + 1);

            List<TopsisService.Alternative> alts = rows.stream()
                    .map(row -> new TopsisService.Alternative(row.request(), row.urgency().getValue(), row.impact().getValue()))
                    .toList();
            List<TopsisService.TopsisResult> ranked = topsisService.rank(alts, wUrgency, wImpact);

            resultArea.removeAll();
            resultArea.add(new H4("Sonuç (yakınlık katsayısına göre sıralı)"));
            int rankNo = 1;
            for (TopsisService.TopsisResult res : ranked) {
                Span line = new Span(rankNo + ". #" + res.request().getId() + " " + res.request().getTitle()
                        + "  ·  Yakınlık: " + res.closeness());
                line.getStyle().set("display", "block").set("margin-bottom", "0.3rem");
                Button approve = new Button("Onayla", ev -> {
                    prioritizationService.saveTopsisResult(res.request(), res.urgency(), res.impact(),
                            res.closeness(), authService.currentUser());
                    Notification.show("#" + res.request().getId() + " TOPSIS ile önceliklendirildi")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refresh();
                });
                approve.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                HorizontalLayout resultRow = new HorizontalLayout(line, approve);
                resultRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
                resultArea.add(resultRow);
                rankNo++;
            }
        });
        rank.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(rank);

        d.add(content);
        d.getFooter().add(new Button("Kapat", e -> d.close()));
        d.open();
    }

    /** SIMPLE / AHP secimi -> Strategy Pattern arka planda */
    private void openPrioritizeDialog(Request r) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Önceliklendir: " + r.getTitle());

        Select<String> method = new Select<>();
        method.setLabel("Yöntem");
        method.setItems("SIMPLE", "AHP");
        method.setValue("SIMPLE");

        IntegerField urgency = new IntegerField("Aciliyet (1-5)");
        urgency.setMin(1); urgency.setMax(5); urgency.setValue(3); urgency.setStepButtonsVisible(true);
        IntegerField impact = new IntegerField("Etki (1-5)");
        impact.setMin(1); impact.setMax(5); impact.setValue(3); impact.setStepButtonsVisible(true);

        NumberField ahpCmp = new NumberField("AHP: Etki / Aciliyet önem oranı (Saaty 1-9)");
        ahpCmp.setMin(0.12); ahpCmp.setMax(9); ahpCmp.setValue(3.0); ahpCmp.setStep(0.5);
        ahpCmp.setVisible(false);
        Span crInfo = new Span("2 kriterli AHP matrisi her zaman tutarlıdır (CR=0 ≤ 0.10)");
        crInfo.setVisible(false);

        method.addValueChangeListener(e -> {
            boolean ahp = "AHP".equals(e.getValue());
            ahpCmp.setVisible(ahp);
            crInfo.setVisible(ahp);
        });

        Button ok = new Button("Kaydet", e -> {
            Prioritization p = prioritizationService.prioritize(r,
                    urgency.getValue(), impact.getValue(),
                    method.getValue(), ahpCmp.getValue() == null ? 3.0 : ahpCmp.getValue(),
                    authService.currentUser());
            Notification.show("Skor: " + p.getPriorityScore());
            d.close();
            refresh();
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        d.add(new VerticalLayout(method, urgency, impact, ahpCmp, crInfo));
        d.getFooter().add(new Button("İptal", e -> d.close()), ok);
        d.open();
    }

    private void openTaskDialog(Request r) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Göreve Dönüştür: " + r.getTitle());

        TextField taskTitle = new TextField("Görev başlığı");
        taskTitle.setValue(r.getTitle());
        taskTitle.setWidth("350px");
        ComboBox<AppUser> dev = new ComboBox<>("Yazılımcı (opsiyonel)");
        dev.setItems(userRepo.findByRole_RoleName(RoleName.DEVELOPER));
        dev.setItemLabelGenerator(AppUser::getNameSurname);
        DatePicker due = new DatePicker("Termin tarihi (opsiyonel)");

        Button ok = new Button("Oluştur", e -> {
            taskService.convertToTask(r, taskTitle.getValue(), dev.getValue(), due.getValue(), authService.currentUser());
            Notification.show("Görev oluşturuldu");
            d.close();
            refresh();
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        d.add(new VerticalLayout(taskTitle, dev, due));
        d.getFooter().add(new Button("İptal", e -> d.close()), ok);
        d.open();
    }

    private void refresh() {
        List<Request> items = (filter == null)
                ? requestService.all()
                : requestService.byStatuses(List.of(filter));
        grid.setItems(items);
    }
}