package com.monad.talep.service.scoring;

import org.springframework.stereotype.Component;

/** Basit mod: Oncelik Skoru = Aciliyet x Etki (1-25) */
@Component("SIMPLE")
public class SimpleScoringStrategy implements ScoringStrategy {

    @Override
    public ScoringResult calculate(int urgency, int impact) {
        return ScoringResult.simple(urgency * impact);
    }

    @Override
    public String getMethodName() { return "SIMPLE"; }
}
