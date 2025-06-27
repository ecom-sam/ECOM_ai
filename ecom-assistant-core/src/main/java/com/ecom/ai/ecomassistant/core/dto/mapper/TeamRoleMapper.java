package com.ecom.ai.ecomassistant.core.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleUpdateCommand;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamRoleMapper {

    TeamRoleMapper INSTANCE = Mappers.getMapper(TeamRoleMapper.class);

    @Mapping(target = "userCount", constant = "0")
    TeamRoleDto toDto(TeamRole teamRole);

    @Mapping(source = "userCount", target = "userCount")
    TeamRoleDto toDto(TeamRole teamRole, Integer userCount);

    TeamRole toTeamRole(TeamRoleCreateCommand command);

    TeamRole toTeamRole(@MappingTarget TeamRole role, TeamRoleUpdateCommand command);
}
