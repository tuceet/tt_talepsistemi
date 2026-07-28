package com.monad.talep.service.scoring;

/** Hesaplama sonucu: skor + AHP metaverileri */
public record ScoringResult(
        double score,
        double weightImpact,
        double weightUrgency,
        double consistencyRatio) {

    public static ScoringResult simple(double score) {
        return new ScoringResult(score, 0, 0, 0);
    }
}
