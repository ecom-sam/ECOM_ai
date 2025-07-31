package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.common.annotation.CurrentUserId;
import com.ecom.ai.ecomassistant.core.dto.command.SendUserMessageCommand;
import com.ecom.ai.ecomassistant.core.service.chat.ChatService;
import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.db.service.ChatTopicService;
import com.ecom.ai.ecomassistant.model.dto.mapper.ChatTopicMapper;
import com.ecom.ai.ecomassistant.model.dto.mapper.MessageCommandMapper;
import com.ecom.ai.ecomassistant.model.dto.request.ChatMessageRequest;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicCreateRequest;
import com.ecom.ai.ecomassistant.model.dto.request.ChatTopicUpdateRequest;
import com.ecom.ai.ecomassistant.model.dto.response.PageResponse;
import com.ecom.ai.ecomassistant.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatService chatService;
    private final ChatTopicService chatTopicService;

    @PostMapping("/topics")
    public ChatTopic createChatTopic(@CurrentUserId String userId, @RequestBody @Valid ChatTopicCreateRequest createRequest) {
        ChatTopic chatTopic = ChatTopicMapper.INSTANCE.toChatTopic(createRequest, userId);
        return chatTopicService.save(chatTopic);
    }

    @GetMapping("/topics")
    public PageResponse<ChatTopic> findAllChatTopicsByUser(
            @CurrentUserId String userId,
            @RequestParam(defaultValue = "") String topic,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdDateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageUtil.buildPageable(page, limit, sortBy, sortDir);
        var pageResult = chatTopicService.search(userId, topic, pageable);
        return PageResponse.of(pageResult);
    }

    @PatchMapping("/topics/{topicId}")
    @RequiresPermissions({"system:chat:access"})
    public ResponseEntity<ChatTopic> updateChatTopic(
            @CurrentUserId String userId, 
            @PathVariable String topicId,
            @RequestBody @Valid ChatTopicUpdateRequest updateRequest) {
        
        // 檢查使用者是否為聊天主題的擁有者
        Optional<ChatTopic> existingTopic = chatTopicService.findById(topicId);
        if (existingTopic.isEmpty() || !existingTopic.get().getCreatedBy().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        ChatTopic chatTopic = ChatTopicMapper.INSTANCE.toChatTopic(updateRequest, userId);
        chatTopic.setId(topicId);  // 確保使用正確的 ID
        return ResponseEntity.ok(chatTopicService.save(chatTopic));
    }

    @PostMapping(value = "/topics/{topicId}/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequiresPermissions({"system:chat:access"})
    public Flux<String> sendMessageToBot(
            @CurrentUserId String userId,
            @PathVariable String topicId,
            @RequestBody @Valid ChatMessageRequest request
    ) {
        // 檢查使用者是否為聊天主題的擁有者
        Optional<ChatTopic> existingTopic = chatTopicService.findById(topicId);
        if (existingTopic.isEmpty() || !existingTopic.get().getCreatedBy().equals(userId)) {
            return Flux.error(new RuntimeException("Access denied: Not topic owner"));
        }
        
        SendUserMessageCommand command = MessageCommandMapper.INSTANCE.toSendUserMessageCommand(request, topicId, userId);
        return chatService.performAiChatFlow(command);
    }

    @GetMapping("/topics/{topicId}/messages")
    @RequiresPermissions({"system:chat:access"})
    public ResponseEntity<List<ChatRecord>> findRecentChatRecord(
            @CurrentUserId String userId,
            @PathVariable String topicId,
            @RequestParam(required = false) String lastChatRecordId,
            @RequestParam(required = false) Integer limit
    ) {
        // 檢查使用者是否為聊天主題的擁有者
        Optional<ChatTopic> existingTopic = chatTopicService.findById(topicId);
        if (existingTopic.isEmpty() || !existingTopic.get().getCreatedBy().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        List<ChatRecord> records = chatService
                .findRecordsByTopicBefore(topicId, lastChatRecordId, limit)
                .reversed();
        return ResponseEntity.ok(records);
    }

}
