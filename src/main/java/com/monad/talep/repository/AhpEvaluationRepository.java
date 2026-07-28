package com.monad.talep.repository;

import com.monad.talep.entity.AhpEvaluation;
import com.monad.talep.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AhpEvaluationRepository extends JpaRepository<AhpEvaluation, Long> {
    Optional<AhpEvaluation> findByRequest(Request request);
}
