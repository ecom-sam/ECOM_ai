package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.repository.ChatRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class ChatRecordService extends CrudService<ChatRecord, String, ChatRecordRepository> {

    protected ChatRecordService(ChatRecordRepository repository) {
        super(repository);
    }

    public List<ChatRecord> findByTopicBefore(String topicId, String chatRecordId, int limit) {
        Assert.notNull(topicId, "topicId cannot be null!");
        Assert.isTrue(limit > 0 && limit <= 50, "limit must between 1 and 50!");
        return repository.findByTopicBefore(topicId, chatRecordId, limit);
    }
}
