package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.db.model.auth.Team;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    @GetMapping
    @RequiresPermissions({"system:team:view"})
    public List<Team> test() {
        return List.of();
    }
}
