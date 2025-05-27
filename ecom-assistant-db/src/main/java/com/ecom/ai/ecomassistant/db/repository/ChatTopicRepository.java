package com.ecom.ai.ecomassistant.db.repository;


import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.List;

public interface ChatTopicRepository extends CouchbaseRepository<ChatTopic, String> {

    List<ChatTopic> findAllByUserId(String userId);
}
