package com.ecom.ai.ecomassistant.core.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.TeamCreateCommand;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {

    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    Team toTeam(TeamCreateCommand teamCreateCommand);
}
