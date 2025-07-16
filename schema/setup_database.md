# Database Setup Guide

Execute the schema files in the following order:

## Prerequisites
確保 Couchbase 容器已經啟動：
```bash
docker ps | grep couchbase-ai
```

## Setup Steps

### 1. 複製 Schema 檔案到容器
```bash
# 從專案根目錄執行
docker cp schema/ couchbase-ai:/tmp/schema/
```

### 2. 執行初始化腳本

#### 方法一：使用 Docker Exec + cbq
```bash
# 1. Initial Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase < /tmp/schema/v0.0_init

# 2. User & RBAC Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase < /tmp/schema/v0.1_user_rbac
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase < /tmp/schema/v0.1_user_rbac_test_data

# 3. Team Roles Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase < /tmp/schema/v0.2_team_role

# 4. System Roles Initialization
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase < /tmp/schema/v0.3_system_role_init
```

#### 方法二：使用 Couchbase Query Workbench
1. 訪問 Couchbase Web Console: http://localhost:8091
2. 進入 Query Workbench
3. 複製每個 schema 檔案的內容並依序執行

## Collections Created

### Core Application
- `document` - File documents and metadata
- `dataset` - Data collections with access control
- `chat-topic` - Chat conversation topics
- `chat-record` - Individual chat interactions
- `chat-message` - Chat messages storage
- `document-vector` - Vector embeddings for AI

### Authentication & Authorization
- `user` - User accounts
- `team` - Teams/organizations
- `team-membership` - User-team relationships
- `team-role` - Team-specific roles
- `system-role` - System-wide roles

### System
- `cache` - AI and application caching

## Default Users Created

| Username | Email | Password | System Role |
|----------|-------|----------|-------------|
| super_admin | super_admin@example.com | password123 | SUPER_ADMIN |
| user_admin | user_admin@example.com | password123 | USER_ADMIN |
| team_admin | team_admin@example.com | password123 | TEAM_ADMIN |

## Verification

After setup, verify collections exist:
```sql
SELECT name FROM system:keyspaces 
WHERE bucket_name = 'ECOM' AND scope_name = 'AI';
```

Verify users exist:
```sql
SELECT name, email, systemRoles FROM `ECOM`.`AI`.`user`;
```