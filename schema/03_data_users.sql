-- Insert user-related initial data
-- This file contains UPSERT statements for user, team, and dataset collections

-- Insert test users
UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`user` (KEY, VALUE)
VALUES (
  "super_admin",
  {
    "name": "super_admin",
    "email": "super_admin@example.com",
    "password": "$2a$10$zMZV6dqULUBrmpMvIVqwwuKPflehTqzG.O//VtXNoBLwnyULBawJS",
    "status": "ACTIVE",
    "systemRoles": ["SUPER_ADMIN"],
    "teamMembershipIds": [],
    "createdAt": "2024-06-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-06-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.User"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`user` (KEY, VALUE)
VALUES (
  "user_admin",
  {
    "name": "user_admin",
    "email": "user_admin@example.com",
    "password": "$2a$10$w5hq6A2EM9Jr/70R/skvSewiAZx7wOCZwuyaw6mlaIQh0ByEhe19u",
    "status": "ACTIVE",
    "systemRoles": ["USER_ADMIN"],
    "teamMembershipIds": [],
    "createdAt": "2024-06-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-06-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.User"
  }
);

UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`user` (KEY, VALUE)
VALUES (
  "team_admin",
  {
    "name": "team_admin",
    "email": "team_admin@example.com",
    "password": "$2a$10$oYG.W3902kHxOiUk2dO.ZOQbPveCcN5XKDgh4myyBYuFJJbADbzNO",
    "status": "ACTIVE",
    "systemRoles": ["TEAM_ADMIN"],
    "teamMembershipIds": [],
    "createdAt": "2024-06-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-06-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.User"
  }
);

-- Insert sample team for testing
UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`team` (KEY, VALUE)
VALUES (
  "default_team",
  {
    "id": "default_team",
    "name": "預設團隊",
    "description": "系統預設團隊",
    "ownerId": "super_admin",
    "active": true,
    "createdAt": "2024-06-16T10:00:00Z",
    "createdBy": "system",
    "updatedAt": "2024-06-16T10:00:00Z",
    "updatedBy": "system",
    "_class": "com.ecom.ai.ecomassistant.db.model.auth.Team"
  }
);

-- Insert sample dataset for testing
UPSERT INTO `${COUCHBASE_BUCKET_NAME}`.`${COUCHBASE_SCOPE_NAME}`.`dataset` (KEY, VALUE)
VALUES (
  "sample_dataset",
  {
    "id": "sample_dataset",
    "name": "範例知識庫",
    "description": "系統範例知識庫",
    "teamId": "default_team",
    "accessType": "PRIVATE",
    "authorizedTeamIds": ["default_team"],
    "createdAt": "2024-06-16T10:00:00Z",
    "createdBy": "super_admin",
    "updatedAt": "2024-06-16T10:00:00Z",
    "updatedBy": "super_admin",
    "_class": "com.ecom.ai.ecomassistant.db.model.Dataset"
  }
);