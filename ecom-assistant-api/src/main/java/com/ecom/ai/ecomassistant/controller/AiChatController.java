package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.core.service.chat.ChatService;
import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.model.dto.mapper.MessageCommandMapper;
import com.ecom.ai.ecomassistant.model.dto.request.ChatMessageRequest;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatService chatService;

    @PostMapping("/topics")
    public ChatTopic createChatTopic(@RequestBody @Valid ChatTopicCreateRequest createRequest, @CurrentUserId String userId) {
        return chatService.createChatTopic(createRequest.getTopic(), userId);
    }

    @GetMapping("/topics")
    public List<ChatTopic> findAllChatTopicsByUser(@CurrentUserId String userId) {
        return chatService.findAllChatTopicsByUser(userId);
    }

    @PostMapping(value = "/topics/{topicId}/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageToBot(
            @PathVariable String topicId,
            @RequestBody @Valid ChatMessageRequest request,
            @CurrentUserId String userId
    ) {
        SendUserMessageCommand command = MessageCommandMapper.INSTANCE.toSendUserMessageCommand(request, topicId, userId);
        return chatService.performAiChatFlow(command);
    }

}
