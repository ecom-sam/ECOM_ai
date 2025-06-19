package com.ecom.ai.ecomassistant.db.repository.auth;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.auth.Team;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface TeamRepository extends CouchbaseRepository<Team, String> {

    List<Team> findAllById(Set<String> ids);
}
