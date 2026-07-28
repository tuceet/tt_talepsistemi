package com.monad.talep.repository;

import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {
    List<RequestStatusHistory> findByRequestOrderByChangedAtAsc(Request request);
}
