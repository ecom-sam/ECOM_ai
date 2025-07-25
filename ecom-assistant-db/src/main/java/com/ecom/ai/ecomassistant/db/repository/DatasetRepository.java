package com.ecom.ai.ecomassistant.db.repository;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
}
