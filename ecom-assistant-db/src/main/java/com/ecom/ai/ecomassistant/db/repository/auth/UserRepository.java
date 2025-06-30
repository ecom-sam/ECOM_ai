package com.ecom.ai.ecomassistant.db.repository.auth;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.auth.User;
import com.ecom.ai.ecomassistant.db.model.dto.UserInfo;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface UserRepository extends CouchbaseRepository<User, String> {

    @Query("SELECT META().id as __id, name, email, status " +
            "FROM #{#n1ql.collection} " +
            "WHERE #{#n1ql.filter} " +
            "AND contains(lower(`email`), $filter) " +
            "LIMIT 20"
    )

    List<UserInfo> listEmail(String filter);

    Optional<User> findByEmail(String email);

    @Query("#{#n1ql.selectEntity} " +
            "WHERE #{#n1ql.filter} " +
            "AND ( " +
            "contains(lower(`name`), $filter) OR contains(lower(`email`), $filter) " +
            ")"
    )
    Page<User> searchByCriteria(String filter, Pageable pageable);
}
