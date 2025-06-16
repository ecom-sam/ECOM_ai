package com.ecom.ai.ecomassistant.controller;

import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.shiro.authz.annotation.Logical.OR;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    @GetMapping
    @RequiresPermissions(logical = OR, value = {"system:admin", "system:team_create"})
    public void test() {

    }
}
