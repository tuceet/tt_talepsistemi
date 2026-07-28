package com.monad.talep.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ttt_prioritizations")
public class Prioritization {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "priority_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", unique = true)
    private Request request;

    @Column(nullable = false)
    private int urgency;   // 1-5

    @Column(nullable = false)
    private int impact;    // 1-5

    @Column(name = "priority_score")
    private double priorityScore;

    @Column(nullable = false, length = 10)
    private String method; // SIMPLE / AHP

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluated_by")
    private AppUser evaluatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Request getRequest() { return request; }
    public void setRequest(Request v) { this.request = v; }
    public int getUrgency() { return urgency; }
    public void setUrgency(int v) { this.urgency = v; }
    public int getImpact() { return impact; }
    public void setImpact(int v) { this.impact = v; }
    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double v) { this.priorityScore = v; }
    public String getMethod() { return method; }
    public void setMethod(String v) { this.method = v; }
    public AppUser getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(AppUser v) { this.evaluatedBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
