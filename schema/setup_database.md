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
docker cp schema_generated/. couchbase-ai:/tmp/schema/
```

### 3. 執行初始化腳本

執行 `generate_schema.sh` 後會自動產生 `schema_generated/init_couchbase.sh` 初始化腳本，該腳本包含完整的資料庫初始化流程。

#### 自動執行初始化 (推薦)
```bash
# 執行自動產生的初始化腳本
bash schema_generated/init_couchbase.sh
```

此腳本會自動執行：
1. 檢查 Couchbase 容器狀態
2. 透過 REST API 建立 Bucket
3. 在單一 docker exec 指令中依序執行：
   - 建立 Scope
   - 執行所有 schema 檔案 (scopes → collections → data → indexes)
4. 顯示完成狀態和驗證資訊

**優化特色**：使用單一 docker exec 指令執行所有資料庫初始化，減少容器啟動開銷，提升執行效率。

#### 手動執行 (進階用戶)
如果需要手動控制，可以：
1. 訪問 Couchbase Web Console: http://localhost:8091
2. 使用你的帳密登入 (根據 .env 設定)
3. 進入 Query Workbench
4. 依序複製每個 `schema_generated/` 資料夾中的檔案內容並執行

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
| super_admin | super_admin@example.com | super_admin | SUPER_ADMIN |
| user_admin | user_admin@example.com | user_admin | USER_ADMIN |
| team_admin | team_admin@example.com | team_admin | TEAM_ADMIN |

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