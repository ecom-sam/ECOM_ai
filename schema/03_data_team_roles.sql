-- Insert team role data
-- This file contains UPSERT statements for team-role collection

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role` (KEY, VALUE)
VALUES (
  "team-admin",
  {
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.TeamRole",
    "id": "team-admin",
    "name": "團隊管理者",
    "description": "團隊管理者",
    "isSystemRole": true,
    "permissions": [
      "team:*",
      "dataset:*"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role` (KEY, VALUE)
VALUES (
  "dataset-manager",
  {
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.TeamRole",
    "id": "dataset-manager",
    "name": "知識庫管理者",
    "description": "可執行所有知識庫操作",
    "isSystemRole": true,
    "permissions": [
      "dataset:*"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role` (KEY, VALUE)
VALUES (
  "dataset-contributor",
  {
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.TeamRole",
    "id": "dataset-contributor",
    "name": "知識庫貢獻者",
    "description": "知識庫貢獻者",
    "isSystemRole": true,
    "permissions": [
      "dataset:view",
      "dataset:file:upload"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team-role` (KEY, VALUE)
VALUES (
  "team-member",
  {
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.TeamRole",
    "id": "team-member",
    "name": "團隊成員",
    "description": "一般成員",
    "isSystemRole": true,
    "permissions": [
      "team:view",
      "dataset:view"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system"
  }
);