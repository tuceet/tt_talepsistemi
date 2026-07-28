package com.monad.talep.service;

import com.monad.talep.entity.*;
import com.monad.talep.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RequestService {

    private static final List<RequestStatus> OPEN_STATUSES =
            List.of(RequestStatus.NEW, RequestStatus.UNDER_REVIEW, RequestStatus.PRIORITIZED);

    private final RequestRepository requestRepo;
    private final RequestStatusHistoryRepository historyRepo;
    private final CommentRepository commentRepo;
    private final NotificationService notificationService;
    private final ActivityLogService logService;

    public RequestService(RequestRepository requestRepo,
                          RequestStatusHistoryRepository historyRepo,
                          CommentRepository commentRepo,
                          NotificationService notificationService,
                          ActivityLogService logService) {
        this.requestRepo = requestRepo;
        this.historyRepo = historyRepo;
        this.commentRepo = commentRepo;
        this.notificationService = notificationService;
        this.logService = logService;
    }

    /** Musterinin acik (NEW/UNDER_REVIEW/PRIORITIZED) talep sayisi. */
    public long openRequestCount(AppUser customer) {
        return requestRepo.countByCustomerAndStatusIn(customer, OPEN_STATUSES);
    }

    /** Plan limitine gore yeni talep olusturulabilir mi. */
    public boolean canCreateRequest(AppUser customer) {
        int limit = customer.getPlan().getMaxOpenRequests();
        return customer.getPlan().isUnlimited() || openRequestCount(customer) < limit;
    }

    @Transactional
    public Request createRequest(AppUser customer, Project project, String title, String description) {
        if (!canCreateRequest(customer)) {
            throw new PlanLimitExceededException(customer.getPlan());
        }
        Request r = new Request();
        r.setCustomer(customer);
        r.setProject(project);
        r.setTitle(title);
        r.setDescription(description);
        r.setStatus(RequestStatus.NEW);
        r = requestRepo.save(r);

        addHistory(r, null, RequestStatus.NEW, customer);
        logService.log(customer, "CREATE_REQUEST", "REQUEST", r.getId(), title);
        return r;
    }

    @Transactional
    public void changeStatus(Request request, RequestStatus newStatus, AppUser changedBy) {
        RequestStatus old = request.getStatus();
        request.setStatus(newStatus);
        requestRepo.save(request);
        addHistory(request, old, newStatus, changedBy);
        logService.log(changedBy, "STATUS_CHANGE", "REQUEST", request.getId(), old + " -> " + newStatus);
        notificationService.notify(request.getCustomer(),
                "Talebiniz (#" + request.getId() + ") durumu: " + newStatus);
    }

    private void addHistory(Request r, RequestStatus oldS, RequestStatus newS, AppUser by) {
        RequestStatusHistory h = new RequestStatusHistory();
        h.setRequest(r);
        h.setOldStatus(oldS);
        h.setNewStatus(newS);
        h.setChangedBy(by);
        historyRepo.save(h);
    }

    @Transactional
    public void addComment(Request request, AppUser user, String content) {
        CommentEntity c = new CommentEntity();
        c.setRequest(request);
        c.setUser(user);
        c.setContent(content);
        commentRepo.save(c);
        logService.log(user, "COMMENT", "REQUEST", request.getId(), "Yorum eklendi");
    }

    public List<Request> myRequests(AppUser customer) {
        return requestRepo.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public List<Request> byStatuses(List<RequestStatus> statuses) {
        return requestRepo.findByStatusInOrderByCreatedAtDesc(statuses);
    }

    public List<Request> all() { return requestRepo.findAll(); }

    public List<CommentEntity> comments(Request r) { return commentRepo.findByRequestOrderByCreatedAtAsc(r); }

    public List<RequestStatusHistory> history(Request r) { return historyRepo.findByRequestOrderByChangedAtAsc(r); }

    public long countByStatus(RequestStatus s) { return requestRepo.countByStatus(s); }

    public long countMineByStatus(AppUser customer, RequestStatus s) {
        return requestRepo.countByCustomerAndStatus(customer, s);
    }

    /** Musteri plan limitini asinca firlatilir (ust katmanda yakalanir). */
    public static class PlanLimitExceededException extends RuntimeException {
        private final PlanType plan;
        public PlanLimitExceededException(PlanType plan) {
            super("Plan limiti asildi: " + plan.getDisplayName() + " (" + plan.getMaxOpenRequests() + " acik talep)");
            this.plan = plan;
        }
        public PlanType getPlan() { return plan; }
    }
}
