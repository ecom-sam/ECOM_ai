package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.TeamMembersInviteCommand;
import com.ecom.ai.ecomassistant.model.dto.request.TeamMembersInviteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMemberRequestMapper {

    TeamMemberRequestMapper INSTANCE = Mappers.getMapper(TeamMemberRequestMapper.class);

    @Mapping(source = "teamId", target = "teamId")
    TeamMembersInviteCommand toInviteCommand(TeamMembersInviteRequest request, String teamId);
}
