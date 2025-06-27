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
import java.util.Optional;
import java.util.Set;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface TeamMembershipRepository extends CouchbaseRepository<TeamMembership, String>  {

    String teamMemberDtoQuery = """
           SELECT '' as __id, tm.teamId AS teamId, tm.teamRoles AS teamRoles,
           {
             "id": META(u).id,
             "name": u.name,
             "email": u.email,
             "status": u.status
           } AS user
           FROM `team-membership` tm
           JOIN `user` u ON KEYS tm.userId
           WHERE tm.teamId = $teamId
           """;

    @Query("""
            SELECT '' AS __id, tm.teamId AS teamId, COUNT(*) AS count
            FROM #{#n1ql.collection} tm
            WHERE #{#n1ql.filter}
            AND tm.teamId IN $teamIds
            GROUP BY tm.teamId
            """
    )
    List<TeamUserCount> countGroupedByTeamId(Set<String> teamIds);

    @Query(teamMemberDtoQuery)
    List<TeamMemberDto> findAllDtoByTeamId(String teamId);

    @Query(teamMemberDtoQuery + " AND tm.userId = $userId")
    List<TeamMemberDto> findDtoByTeamIdAndUserId(String teamId, String userId);

    List<TeamMembership> findAllByUserId(String userId);

    Optional<TeamMembership> findByTeamIdAndUserId(String teamId, String userId);

    List<TeamMembership> findAllByTeamIdAndUserIdIn(String teamId, List<String> userIds);
}
