package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.TeamCreateCommand;
import com.ecom.ai.ecomassistant.model.dto.request.TeamCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamRequestMapper {

    TeamRequestMapper INSTANCE = Mappers.getMapper(TeamRequestMapper.class);

    TeamCreateCommand toCreateTeamCommand(TeamCreateRequest createRequest);
}
