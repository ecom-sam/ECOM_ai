package com.ecom.ai.ecomassistant.db.repository;


import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.ChatTopic;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface ChatTopicRepository extends CouchbaseRepository<ChatTopic, String> {

    List<ChatTopic> findAllByUserIdOrderByCreatedDateTimeDesc(String userId);

    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "AND `userId` = $userId " +
            "AND contains(lower(`topic`), $topic)"
    )
    Page<ChatTopic> searchByCriteria(String userId, String topic, Pageable pageable);

}
