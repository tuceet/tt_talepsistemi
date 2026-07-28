package com.monad.talep.repository;

import com.monad.talep.entity.Prioritization;
import com.monad.talep.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PrioritizationRepository extends JpaRepository<Prioritization, Long> {
    Optional<Prioritization> findByRequest(Request request);
}
