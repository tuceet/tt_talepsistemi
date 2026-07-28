package com.monad.talep.service.scoring;

/**
 * STRATEGY PATTERN
 * Onceliklendirme skoru hesaplama stratejisi.
 * SIMPLE: aciliyet x etki | AHP: agirlikli toplam (Saaty)
 */
public interface ScoringStrategy {
    ScoringResult calculate(int urgency, int impact);
    String getMethodName();
}
