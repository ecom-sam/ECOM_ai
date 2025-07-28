package com.ecom.ai.ecomassistant.db.repository;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface DatasetRepository extends CouchbaseRepository<Dataset, String> {

    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "AND contains(lower(`name`), $name)")
    Page<Dataset> searchByCriteria(String name, Pageable pageable);

    @Query("#{#n1ql.selectEntity} " +
            "WHERE accessType = 'PUBLIC' " +
            "OR (accessType = 'GROUP' AND ANY g IN authorizedTeamIds SATISFIES g IN $userTeamIds END) " +
            "OR (accessType = 'PRIVATE' AND createdBy = $userId)" +
            "AND contains(lower(`name`), $name)")
    Page<Dataset> findVisibleDatasets(String name, String userId, Set<String> userTeamIds, Pageable pageable);
    
    // 新增 tags 相關查詢方法（簡化版，不需要複雜的權限查詢）
    
    /**
     * 按 tags 查詢知識庫（供管理員使用）
     */
    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "AND ANY tag IN tags SATISFIES tag IN $tags END")
    List<Dataset> findByTagsIn(List<String> tags);
    
    /**
     * 查詢所有 tags（供管理員使用）
     */
    @Query("SELECT DISTINCT FLATTEN_KEYS(tags, 1) as tag " +
            "FROM #{#n1ql.bucket}.`#{#n1ql.scope}`.`#{#n1ql.collection}` " +
            "WHERE #{#n1ql.filter} " +
            "AND tags IS NOT MISSING " +
            "AND ARRAY_LENGTH(tags) > 0")
    List<String> findDistinctTags();
}
