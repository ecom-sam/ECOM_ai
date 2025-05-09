package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.model.dto.request.UserCreateRequest;
import com.ecom.ai.ecomassistant.db.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(UserCreateRequest userCreateRequest);
}
