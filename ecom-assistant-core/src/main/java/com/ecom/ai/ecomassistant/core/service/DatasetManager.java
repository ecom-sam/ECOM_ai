package com.ecom.ai.ecomassistant.core.service;

import com.ecom.ai.ecomassistant.auth.util.PermissionUtil;
import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.model.auth.TeamMembership;
import com.ecom.ai.ecomassistant.db.service.DatasetService;
import com.ecom.ai.ecomassistant.db.service.auth.TeamMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.ecom.ai.ecomassistant.auth.permission.SystemPermission.SYSTEM_DATASET_ADMIN;

@Service
@RequiredArgsConstructor
public class DatasetManager {

    private final DatasetService datasetService;

    private final TeamMembershipService teamMembershipService;

    public Page<Dataset> findVisibleDatasets(String userId, String name, Pageable pageable) {
        boolean canViewAll = PermissionUtil.hasAnyPermission(Set.of(
                SYSTEM_DATASET_ADMIN.getCode()
        ));

        if (canViewAll) {
            return datasetService.searchAll(name, pageable);
        } else {
            Set<String> userTeamIds = teamMembershipService
                    .findAllByUserId(userId).stream()
                    .map(TeamMembership::getTeamId)
                    .collect(Collectors.toSet());
            return datasetService.findVisibleDatasets(name, userId, userTeamIds, pageable);
        }
    }
}
