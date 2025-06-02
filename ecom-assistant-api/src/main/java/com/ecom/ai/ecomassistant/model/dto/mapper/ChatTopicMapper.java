package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatTopicMapper {

    ChatTopicMapper INSTANCE = Mappers.getMapper(ChatTopicMapper.class);

    ChatTopic toChatTopic(ChatTopicCreateRequest createRequest);

    ChatTopic toChatTopic(ChatTopicUpdateRequest updateRequest);
}
