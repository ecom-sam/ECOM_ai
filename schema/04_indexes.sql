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

-- Chat message collection indexes
CREATE INDEX idx_chat_message_username_topicId ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`chat-message`(username, topicId, datetime) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.ChatMessage";

-- Chat record collection indexes
CREATE INDEX idx_chat_record_topicId ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`chat-record`(topicId, chatRecordId) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.ChatRecord";

-- Chat topic collection indexes
CREATE INDEX idx_chat_topic_userId ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`chat-topic`(userId, createdDateTime) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.ChatTopic";
CREATE INDEX idx_chat_topic_search ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`chat-topic`(userId, lower(topic)) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.ChatTopic";

-- Dataset collection indexes
CREATE INDEX idx_dataset_name ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`dataset`(lower(name)) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.Dataset";
CREATE INDEX idx_dataset_visibility ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`dataset`(accessType, createdBy, authorizedTeamIds) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.Dataset";

-- Document collection indexes
CREATE INDEX idx_document_datasetId ON `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`document`(datasetId) WHERE `_class` = "com.ecom.ai.ecomassistant.db.model.Document";