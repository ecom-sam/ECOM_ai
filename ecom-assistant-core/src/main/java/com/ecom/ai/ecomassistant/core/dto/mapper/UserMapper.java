package com.ecom.ai.ecomassistant.core.dto.mapper;

import com.ecom.ai.ecomassistant.core.dto.response.UserDetailDto;
import com.ecom.ai.ecomassistant.core.dto.response.UserDto;
import com.ecom.ai.ecomassistant.core.dto.response.UserPermissionDto;
import com.ecom.ai.ecomassistant.core.service.UserManager;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    UserDetailDto toUserDetailDto(User user);

    @Mapping(source = "context.roles", target = "roles")
    @Mapping(source = "context.permissions", target = "permissions")
    UserPermissionDto toPermissionDto(User user, UserManager.UserRoleContext context);
}
