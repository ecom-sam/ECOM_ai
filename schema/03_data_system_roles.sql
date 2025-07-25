-- Insert system role data
-- This file contains UPSERT statements for system-role collection

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`system-role` (KEY, VALUE)
VALUES (
  "SUPER_ADMIN",
  {
    "id": "SUPER_ADMIN",
    "name": "系統超級管理員",
    "description": "擁有系統所有權限的超級管理員",
    "permissions": [
      "system:*"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.SystemRole"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`system-role` (KEY, VALUE)
VALUES (
  "USER_ADMIN",
  {
    "id": "USER_ADMIN",
    "name": "使用者管理員",
    "description": "管理使用者帳號的管理員",
    "permissions": [
      "system:user:manage",
      "system:user:view",
      "system:user:create",
      "system:user:update",
      "system:user:delete",
      "system:user:activate",
      "system:user:deactivate"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.SystemRole"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`system-role` (KEY, VALUE)
VALUES (
  "TEAM_ADMIN",
  {
    "id": "TEAM_ADMIN",
    "name": "團隊管理員",
    "description": "管理團隊的管理員",
    "permissions": [
      "system:team:view",
      "system:team:create",
      "system:team:update",
      "system:team:manage",
      "team:*"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.SystemRole"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`system-role` (KEY, VALUE)
VALUES (
  "REGULAR_USER",
  {
    "id": "REGULAR_USER",
    "name": "一般使用者",
    "description": "系統的一般使用者",
    "permissions": [
      "system:user:view:self",
      "system:user:update:self"
    ],
    "createdAt": "2024-07-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-07-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.SystemRole"
  }
);