package com.ecom.ai.ecomassistant.db.service;

import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import com.ecom.ai.ecomassistant.db.repository.ChatRecordRepository;

public class ChatRecordService extends CrudService<ChatRecord, String, ChatRecordRepository> {

    protected ChatRecordService(ChatRecordRepository repository) {
        super(repository);
    }
}
