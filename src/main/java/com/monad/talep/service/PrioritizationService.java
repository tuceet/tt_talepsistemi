package com.monad.talep.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monad.talep.entity.AhpEvaluation;
import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Prioritization;
import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatus;
import com.monad.talep.repository.AhpEvaluationRepository;
import com.monad.talep.repository.PrioritizationRepository;
import com.monad.talep.service.scoring.AhpScoringStrategy;
import com.monad.talep.service.scoring.ScoringResult;
import com.monad.talep.service.scoring.ScoringStrategy;

@Service
public class PrioritizationService {

    private final Map<String, ScoringStrategy> strategies; // Spring bean adiyla enjekte eder: SIMPLE, AHP
    private final PrioritizationRepository priRepo;
    private final AhpEvaluationRepository ahpRepo;
    private final RequestService requestService;
    private final ActivityLogService logService;

    public PrioritizationService(Map<String, ScoringStrategy> strategies,
                                 PrioritizationRepository priRepo,
                                 AhpEvaluationRepository ahpRepo,
                                 RequestService requestService,
                                 ActivityLogService logService) {
        this.strategies = strategies;
        this.priRepo = priRepo;
        this.ahpRepo = ahpRepo;
        this.requestService = requestService;
        this.logService = logService;
    }

    /**
     * STRATEGY PATTERN kullanim noktasi:
     * method = "SIMPLE" veya "AHP" -> dogru strateji map'ten secilir.
     */
    @Transactional
    public Prioritization prioritize(Request request, int urgency, int impact,
                                     String method, double ahpComparison, AppUser evaluator) {

        ScoringStrategy strategy = strategies.get(method);
        if (strategy == null) throw new IllegalArgumentException("Bilinmeyen yontem: " + method);

        if (strategy instanceof AhpScoringStrategy ahp) {
            ahp.setComparisonValue(ahpComparison);
        }

        ScoringResult result = strategy.calculate(urgency, impact);

        Prioritization p = priRepo.findByRequest(request).orElseGet(Prioritization::new);
        p.setRequest(request);
        p.setUrgency(urgency);
        p.setImpact(impact);
        p.setMethod(method);
        p.setPriorityScore(result.score());
        p.setEvaluatedBy(evaluator);
        p = priRepo.save(p);

        if ("AHP".equals(method)) {
            AhpEvaluation e = ahpRepo.findByRequest(request).orElseGet(AhpEvaluation::new);
            e.setRequest(request);
            e.setComparisonValue(ahpComparison);
            e.setWeightImpact(result.weightImpact());
            e.setWeightUrgency(result.weightUrgency());
            e.setConsistencyRatio(result.consistencyRatio());
            e.setFinalScore(result.score());
            e.setEvaluatedBy(evaluator);
            ahpRepo.save(e);
        }

        requestService.changeStatus(request, RequestStatus.PRIORITIZED, evaluator);
        logService.log(evaluator, "PRIORITIZE", "REQUEST", request.getId(),
                method + " skor=" + result.score());
        return p;
    }

  public Optional<Prioritization> of(Request r) { return priRepo.findByRequest(r); }

    public Optional<AhpEvaluation> ahpOf(Request r) { return ahpRepo.findByRequest(r); }

    /**
     * TOPSIS toplu siralamasindan gelen sonucu kaydeder.
     * TOPSIS coklu-alternatif kiyaslamasi oldugundan Strategy map'inden gecmez;
     * kapanma katsayisi (closeness, 0-1) dogrudan priorityScore olarak saklanir.
     */
    @Transactional
    public Prioritization saveTopsisResult(Request request, int urgency, int impact, double closeness, AppUser evaluator) {
        Prioritization p = priRepo.findByRequest(request).orElseGet(Prioritization::new);
        p.setRequest(request);
        p.setUrgency(urgency);
        p.setImpact(impact);
        p.setMethod("TOPSIS");
        p.setPriorityScore(closeness);
        p.setEvaluatedBy(evaluator);
        p = priRepo.save(p);

        requestService.changeStatus(request, RequestStatus.PRIORITIZED, evaluator);
        logService.log(evaluator, "PRIORITIZE", "REQUEST", request.getId(), "TOPSIS skor=" + closeness);
        return p;
    }
}

