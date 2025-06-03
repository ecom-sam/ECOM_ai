package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatTopicMapper {

    ChatTopicMapper INSTANCE = Mappers.getMapper(ChatTopicMapper.class);

    @Mapping(source = "userId", target = "userId")
    ChatTopic toChatTopic(ChatTopicCreateRequest createRequest, String userId);

    @Mapping(source = "userId", target = "userId")
    ChatTopic toChatTopic(ChatTopicUpdateRequest updateRequest, String userId);
}
