package com.ecom.ai.ecomassistant.db.repository.auth;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface TeamRepository extends CouchbaseRepository<Team, String> {
    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "ORDER BY name ASC")
    List<Team> findAllWithSort();

    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "AND META().id IN $ids " +
            "ORDER BY name ASC")
    List<Team> findAllWithSort(Set<String> ids);
}
