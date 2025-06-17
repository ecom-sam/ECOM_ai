package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import com.ecom.ai.ecomassistant.db.repository.ChatTopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatTopicService extends CrudService<ChatTopic, String, ChatTopicRepository> {

    protected ChatTopicService(ChatTopicRepository repository) {
        super(repository);
    }

    public List<ChatTopic> findAllByUserId(String userId) {
        return repository.findAllByUserIdOrderByCreatedDateTimeDesc(userId);
    }

    public Page<ChatTopic> search(String userId, String topic, Pageable pageable) {
        return repository.searchByCriteria(userId, topic, pageable);
    }
}
