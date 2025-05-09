package com.ecom.ai.ecomassistant.model.mapper;

import com.ecom.ai.ecomassistant.model.User;
import com.ecom.ai.ecomassistant.model.dto.request.UserCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(UserCreateRequest userCreateRequest);
}
