package com.ecom.ai.ecom_assistant.repository;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecom_assistant.model.User;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface UserRepository extends CouchbaseRepository<User, String> {
}
