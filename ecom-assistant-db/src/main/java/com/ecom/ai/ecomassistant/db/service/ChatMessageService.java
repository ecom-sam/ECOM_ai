package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import com.ecom.ai.ecomassistant.db.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService extends CrudService<ChatMessage, String, ChatMessageRepository>{

    protected ChatMessageService(ChatMessageRepository repository) {
        super(repository);
    }

    public ChatMessage createMessage(ChatMessage chatMessage) {
        return repository.save(chatMessage);
    }
}
