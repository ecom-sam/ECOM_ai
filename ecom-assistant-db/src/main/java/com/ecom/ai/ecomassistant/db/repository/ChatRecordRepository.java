package com.ecom.ai.ecomassistant.db.repository;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.ChatRecord;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface ChatRecordRepository extends CouchbaseRepository<ChatRecord, String> {

    @Query("#{#n1ql.selectEntity} WHERE topicId = $1 ORDER BY chatRecordId ASC LIMIT 10")
    List<ChatRecord> findLatestChatByTopicId(String topicId);

    @Query("#{#n1ql.selectEntity} " +
            "WHERE topicId = $topicId " +
            "AND ( $chatRecordId is null or chatRecordId < $chatRecordId ) " +
            "ORDER BY META().id DESC " +
            "LIMIT $limit"
    )
    List<ChatRecord> findByTopicBefore(String topicId, String chatRecordId, int limit);
}
