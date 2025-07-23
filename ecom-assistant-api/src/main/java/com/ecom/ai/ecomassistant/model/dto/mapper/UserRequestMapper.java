package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.command.UserActivateCommand;
import com.ecom.ai.ecomassistant.model.dto.request.UserActivateRequest;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserRequestMapper {

    UserRequestMapper INSTANCE = Mappers.getMapper(UserRequestMapper.class);

    UserActivateCommand toUserActivateCommand(UserActivateRequest request);
}
