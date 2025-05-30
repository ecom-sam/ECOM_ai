package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.core.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.model.dto.request.ChatMessageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageCommandMapper {

    MessageCommandMapper INSTANCE = Mappers.getMapper(MessageCommandMapper.class);

    @Mapping(source = "topicId", target = "topicId")
    @Mapping(source = "userId", target = "userId")
    SendUserMessageCommand toSendUserMessageCommand(ChatMessageRequest request, String topicId, String userId);
}
