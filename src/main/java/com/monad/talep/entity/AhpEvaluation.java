package com.monad.talep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tt_ahp_evaluations")
public class AhpEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ahp_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", unique = true)
    private Request request;

    /** Saaty 1-9: Etki, Aciliyet'ten kac kat onemli (kesirli deger = tersi) */
    @Column(name = "comparison_value", nullable = false)
    private double comparisonValue;

    @Column(name = "weight_impact")   private double weightImpact;
    @Column(name = "weight_urgency")  private double weightUrgency;
    @Column(name = "consistency_ratio") private double consistencyRatio;
    @Column(name = "final_score")     private double finalScore;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluated_by")
    private AppUser evaluatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Request getRequest() { return request; }
    public void setRequest(Request v) { this.request = v; }
    public double getComparisonValue() { return comparisonValue; }
    public void setComparisonValue(double v) { this.comparisonValue = v; }
    public double getWeightImpact() { return weightImpact; }
    public void setWeightImpact(double v) { this.weightImpact = v; }
    public double getWeightUrgency() { return weightUrgency; }
    public void setWeightUrgency(double v) { this.weightUrgency = v; }
    public double getConsistencyRatio() { return consistencyRatio; }
    public void setConsistencyRatio(double v) { this.consistencyRatio = v; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double v) { this.finalScore = v; }
    public AppUser getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(AppUser v) { this.evaluatedBy = v; }
}
