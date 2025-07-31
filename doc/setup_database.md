# Database Setup Guide

Execute the schema files in the following order:

## Prerequisites

### 1. 啟動 Couchbase
```bash
docker run -d \
  --name couchbase-ai \
  --hostname couchbase.local \
  --add-host couchbase.local:127.0.0.1 \
  -p 8091-8097:8091-8097 \
  -p 9123:9123 \
  -p 11210:11210 \
  -p 11280:11280 \
  -p 18091-18097:18091-18097 \
  couchbase:enterprise-7.6.5
```

### 2. 初次設定 Cluster
**重要**: 初次啟動需要設定 Cluster：
1. 訪問 http://localhost:8091
2. 選擇 "Setup New Cluster"
3. 設定管理員帳號密碼（建議與 .env 中的設定一致）

### 3. 確認 Couchbase 容器狀態
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
- 將模板中的佔位符替換為你的實際設定值
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
3. 建立 Scope
4. 依序執行所有 schema 檔案
5. 顯示完成狀態和驗證資訊

#### 手動執行 (進階用戶)
如果需要手動控制，可以透過以下方式：

**透過 Web Console**
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
腳本執行後會在 `schema_generated/` 資料夾中產生對應的檔案，所有模板中的佔位符會被替換為你在 `.env` 檔案中設定的實際值。

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