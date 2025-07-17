-- Create all database indexes
-- This file should be executed last to create all required indexes
-- Note: CREATE INDEX does not support IF NOT EXISTS, so this file should only be run once

-- User collection indexes
CREATE INDEX idx_user_email ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`user`(email) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.User";
CREATE INDEX idx_user_status ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`user`(status) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.User";

-- Team collection indexes
CREATE INDEX idx_team_active ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team`(active) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.Team";

-- Team membership collection indexes
CREATE INDEX idx_team_membership_user ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-membership`(userId) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.TeamMembership";
CREATE INDEX idx_team_membership_team ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-membership`(teamId) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.TeamMembership";

-- Team role collection indexes
CREATE INDEX idx_team_role_class_teamId ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role`(`_class`, `teamId`) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.TeamRole";
CREATE INDEX idx_team_role_system ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role`(`isSystemRole`) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.auth.TeamRole";