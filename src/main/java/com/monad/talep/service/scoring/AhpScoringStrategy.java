package com.monad.talep.service.scoring;

import org.springframework.stereotype.Component;

/**
 * AHP (Analytic Hierarchy Process) modu.
 * 2 kriter: Etki (Impact) ve Aciliyet (Urgency).
 * PO ikili karsilastirma degeri girer (Saaty 1-9):
 *   c = Etki'nin Aciliyet'e gore onemi (3 => Etki 3 kat onemli)
 * 2x2 matris:
 *   [ 1    c ]
 *   [ 1/c  1 ]
 * Agirliklar (geometrik ortalama normalize):
 *   w_impact  = c / (c + 1)
 *   w_urgency = 1 / (c + 1)
 * Not: 2 kriterli matris her zaman tutarlidir -> CR = 0 (Saaty esigi 0.10'un altinda).
 * Final skor = w_impact * impact + w_urgency * urgency  (1-5 olcek)
 */
@Component("AHP")
public class AhpScoringStrategy implements ScoringStrategy {

    /** PO ekranindan set edilir; varsayilan: Etki 3 kat onemli */
    private double comparisonValue = 3.0;

    public void setComparisonValue(double c) {
        if (c < 1.0 / 9 || c > 9) throw new IllegalArgumentException("Saaty degeri 1/9 - 9 araliginda olmali");
        this.comparisonValue = c;
    }

    public double getComparisonValue() { return comparisonValue; }

    @Override
    public ScoringResult calculate(int urgency, int impact) {
        double c = comparisonValue;
        double wImpact = c / (c + 1);
        double wUrgency = 1 / (c + 1);
        double score = wImpact * impact + wUrgency * urgency;
        double cr = 0.0; // 2x2 matris daima tutarli
        return new ScoringResult(round(score), round(wImpact), round(wUrgency), cr);
    }

    private double round(double v) { return Math.round(v * 10000.0) / 10000.0; }

    @Override
    public String getMethodName() { return "AHP"; }
}
