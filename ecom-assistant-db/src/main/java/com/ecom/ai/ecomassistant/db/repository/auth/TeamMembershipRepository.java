package com.ecom.ai.ecomassistant.db.repository.auth;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.model.dto.TeamMemberDto;
import com.ecom.ai.ecomassistant.db.model.dto.TeamUserCount;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface TeamMembershipRepository extends CouchbaseRepository<TeamMembership, String>  {

    @Query("""
            SELECT '' AS __id, tm.teamId AS teamId, COUNT(*) AS count
            FROM #{#n1ql.collection} tm
            WHERE #{#n1ql.filter}
            AND tm.teamId IN $teamIds
            GROUP BY tm.teamId
            """)
    List<TeamUserCount> countGroupedByTeamId(Set<String> teamIds);

    @Query("""
           SELECT '' as __id, tm.teamId AS teamId,
           {
             "id": META(u).id,
             "name": u.name,
             "email": u.email
           } AS user
           FROM `team-membership` tm
           JOIN `user` u ON KEYS tm.userId
           WHERE tm.teamId = $1
           """)
    List<TeamMemberDto> findAllByTeamId(String teamId);
}
