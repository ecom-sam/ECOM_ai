package com.ecom.ai.ecomassistant.db.repository;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.ChatContentRequest;
import com.ecom.ai.ecomassistant.db.model.ChatMessage;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface ChatMessageRepository extends CouchbaseRepository<ChatMessage, String> {

    @Query("#{#n1ql.selectEntity} WHERE username = $1 AND topicId = $2 ORDER BY datetime DESC LIMIT 4")
    List<ChatContentRequest> findRecentContents(String username, String topicId);
}
