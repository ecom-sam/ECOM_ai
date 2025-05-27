package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.db.repository.ChatTopicRepository;

import java.util.List;

public class ChatTopicService extends CrudService<ChatTopic, String, ChatTopicRepository> {

    protected ChatTopicService(ChatTopicRepository repository) {
        super(repository);
    }

    public List<ChatTopic> findAllByUserId(String userId) {
        return repository.findAllByUserId(userId);
    }
}
