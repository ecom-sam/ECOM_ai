# Database Setup Guide

Execute the schema files in the following order:

## Prerequisites
確保 Couchbase 容器已經啟動：
```bash
docker ps | grep couchbase-ai
```

## Setup Steps

### 1. 產生對應 .env 設定的 Schema 檔案
```bash
# 從專案根目錄執行
bash generate_schema.sh
```

此腳本會：
- 讀取 `.env` 檔案中的 `COUCHBASE_BUCKET_NAME` 和 `COUCHBASE_SCOPE_NAME` 設定
- 將原本的 `ECOM` 和 `AI` 替換為你的實際設定
- 在 `schema_generated/` 資料夾中產生對應的 schema 檔案

### 2. 複製產生的 Schema 檔案到容器
```bash
# 從專案根目錄執行
docker cp schema_generated/ couchbase-ai:/tmp/schema/
```

### 3. 執行初始化腳本

#### 方法一：使用 Docker Exec + cbq
```bash
# 0. Bucket & Scope Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.0_bucket_init

# 1. Initial Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.0_init

# 2. User & RBAC Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.1_user_rbac
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.1_user_rbac_test_data

# 3. Team Roles Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.2_team_role

# 4. System Roles Initialization
docker exec couchbase-ai cbq -e "couchbase://localhost" -u admin -p couchbase -f /tmp/schema/v0.3_system_role_init
```

#### 方法二：使用 Couchbase Query Workbench
1. 訪問 Couchbase Web Console: http://localhost:8091
2. 使用帳密登入：`admin` / `couchbase`
3. 進入 Query Workbench
4. 依序複製每個 `schema_generated/` 資料夾中的檔案內容並執行：
   - `v0.0_bucket_init` (建立 Bucket 和 Scope)
   - `v0.0_init` (建立 Collections)
   - `v0.1_user_rbac` (使用者權限系統)
   - `v0.1_user_rbac_test_data` (測試資料)
   - `v0.2_team_role` (團隊角色)
   - `v0.3_system_role_init` (系統角色)

## Schema Generation Script

### 環境變數設定
腳本會從 `.env` 檔案讀取以下變數：
- `COUCHBASE_BUCKET_NAME` (預設: ECOM)
- `COUCHBASE_SCOPE_NAME` (預設: AI)

### 執行範例
```bash
# 設定環境變數
echo "COUCHBASE_BUCKET_NAME=MyBucket" >> .env
echo "COUCHBASE_SCOPE_NAME=MyScope" >> .env

# 產生 schema 檔案
bash generate_schema.sh
```

### 輸出結果
腳本執行後會在 `schema_generated/` 資料夾中產生對應的檔案，所有的 `ECOM` 會被替換為你的 bucket 名稱，所有的 `AI` 會被替換為你的 scope 名稱。

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