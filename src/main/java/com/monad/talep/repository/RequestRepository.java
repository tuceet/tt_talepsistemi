package com.monad.talep.repository;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.Request;
import com.monad.talep.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByCustomerOrderByCreatedAtDesc(AppUser customer);
    List<Request> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<Request> findByStatusInOrderByCreatedAtDesc(List<RequestStatus> statuses);
    long countByStatus(RequestStatus status);
    long countByCustomerAndStatus(AppUser customer, RequestStatus status);
    long countByCustomerAndStatusIn(AppUser customer, List<RequestStatus> statuses);
}
