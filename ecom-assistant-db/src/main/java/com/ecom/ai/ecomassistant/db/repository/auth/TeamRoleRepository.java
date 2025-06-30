package com.ecom.ai.ecomassistant.db.repository.auth;

import com.couchbase.client.java.query.QueryScanConsistency;
import com.ecom.ai.ecomassistant.db.model.auth.TeamRole;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleDto;
import com.ecom.ai.ecomassistant.db.model.dto.TeamRoleUserCountDto;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface TeamRoleRepository extends CouchbaseRepository<TeamRole, String> {

    List<TeamRole> findAllByTeamId(String teamId);

    List<TeamRole> findByIsSystemRoleTrue();

    @Query("""
               SELECT '' AS __id, META(r).id AS roleId, COUNT(DISTINCT m.userId) AS userCount
               FROM #{#n1ql.collection} r
               LEFT JOIN `team-membership` m
               ON r.teamId = m.teamId
               AND ARRAY_CONTAINS(m.teamRoles, META(r).id)
               WHERE META(r).id IN $1
               GROUP BY META(r).id
    """)
    List<TeamRoleUserCountDto> countUsersByRoleIds(List<String> roleIds);

    @Query("SELECT COUNT(r) " +
            "FROM #{#n1ql.collection} r " +
            "WHERE r.id IN :roleIds " +
            "AND r.id IN :roleIds " +
            "AND (" +
            "      (isSystemRole = true)" +
            "      OR" +
            "      (isSystemRole = false AND teamId = $teamId)" +
            ")"
    )
    int countRoles(String teamId, List<String> roleIds);


    @Query("""
               SELECT '' AS __id,
                  META(r).id AS id,
                  r.name AS name,
                  r.description AS description,
                  r.permissions AS permissions,
                  r.isSystemRole AS isSystemRole,
                  COUNT(DISTINCT m.userId) AS userCount
           FROM `team-role` r
           LEFT JOIN `team-membership` m
             ON ARRAY_CONTAINS(m.teamRoles, META(r).id)
             AND m.teamId = $teamId
           WHERE r.isSystemRole = true
           GROUP BY
               META(r).id,
               r.name,
               r.description,
               r.permissions,
               r.isSystemRole
    """)
    List<TeamRoleDto> findSystemTeamRoleByTeamId(String teamId);

}
