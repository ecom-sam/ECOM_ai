package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleCreateCommand;
import com.ecom.ai.ecomassistant.core.dto.command.TeamRoleUpdateCommand;
import com.ecom.ai.ecomassistant.model.dto.request.TeamRoleCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.TeamRoleUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamRoleRequestMapper {

    TeamRoleRequestMapper INSTANCE = Mappers.getMapper(TeamRoleRequestMapper.class);

    @Mapping(source = "teamId", target = "teamId")
    TeamRoleCreateCommand toCreateTeamRoleCommand(TeamRoleCreateRequest createRequest, String teamId);

    @Mapping(source = "teamId", target = "teamId",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    TeamRoleUpdateCommand toTeamRoleUpdateCommand(TeamRoleUpdateRequest updateRequest, String teamId);

}
