package com.ecom.ai.ecomassistant.db.model.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TeamMemberDto {

    private UserInfo user;

    private String teamId;

    private Set<String> teamRoles = new HashSet<>();
}
