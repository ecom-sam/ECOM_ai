package com.ecom.ai.ecom_assistant.model.mapper;

import com.ecom.ai.ecom_assistant.model.User;
import com.ecom.ai.ecom_assistant.model.dto.request.UserCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(UserCreateRequest userCreateRequest);
}
