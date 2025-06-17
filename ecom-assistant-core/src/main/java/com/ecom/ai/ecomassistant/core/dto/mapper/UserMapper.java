package com.ecom.ai.ecomassistant.core.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);
}
