package com.ecom.ai.ecomassistant.db.service.auth;

import com.ecom.ai.ecomassistant.db.model.auth.Team;
import com.ecom.ai.ecomassistant.db.repository.auth.TeamRepository;
import com.ecom.ai.ecomassistant.db.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TeamService extends CrudService<Team, String, TeamRepository> {

    protected TeamService(TeamRepository repository) {
        super(repository);
    }

    public List<Team> findAllById(Set<String> ids) {
        return repository.findAllById(ids);
    }
}
