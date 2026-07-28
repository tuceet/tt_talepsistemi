package com.monad.talep.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ttt_request_status_history")
public class RequestStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id")
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private RequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private RequestStatus newStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "changed_by")
    private AppUser changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Request getRequest() { return request; }
    public void setRequest(Request v) { this.request = v; }
    public RequestStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(RequestStatus v) { this.oldStatus = v; }
    public RequestStatus getNewStatus() { return newStatus; }
    public void setNewStatus(RequestStatus v) { this.newStatus = v; }
    public AppUser getChangedBy() { return changedBy; }
    public void setChangedBy(AppUser v) { this.changedBy = v; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
