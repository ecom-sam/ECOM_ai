package com.ecom.ai.ecomassistant.db.repository;

import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface ChatRecordRepository extends CouchbaseRepository<ChatRecord, String> {
}
