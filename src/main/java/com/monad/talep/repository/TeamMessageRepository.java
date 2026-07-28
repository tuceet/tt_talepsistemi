package com.monad.talep.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monad.talep.entity.TeamMessage;

public interface TeamMessageRepository extends JpaRepository<TeamMessage, Long> {
    List<TeamMessage> findTop100ByOrderByCreatedAtAsc();
}