package com.monad.talep.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.TeamMessage;
import com.monad.talep.repository.TeamMessageRepository;

@Service
public class TeamMessageService {

    private final TeamMessageRepository repo;

    public TeamMessageService(TeamMessageRepository repo) { this.repo = repo; }

    public List<TeamMessage> latest() { return repo.findTop100ByOrderByCreatedAtAsc(); }

    @Transactional
    public TeamMessage send(AppUser sender, String content) {
        TeamMessage m = new TeamMessage();
        m.setSender(sender);
        m.setContent(content);
        return repo.save(m);
    }
}