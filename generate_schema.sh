#!/bin/bash

# Schema Generator Script
# This script reads .env variables and replaces bucket/scope names in schema files

set -e

# Default values
DEFAULT_BUCKET_NAME="ECOM"
DEFAULT_SCOPE_NAME="AI"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔧 Schema Generator Script${NC}"
echo "================================="

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  Warning: .env file not found. Using default values.${NC}"
    BUCKET_NAME=$DEFAULT_BUCKET_NAME
    SCOPE_NAME=$DEFAULT_SCOPE_NAME
    USERNAME="admin"
    PASSWORD="couchbase"
else
    echo -e "${GREEN}✅ Found .env file${NC}"
    
    # Load environment variables from .env file
    export $(grep -v '^#' .env | xargs)
    
    # Get bucket and scope names from environment variables
    BUCKET_NAME=${COUCHBASE_BUCKET_NAME:-$DEFAULT_BUCKET_NAME}
    SCOPE_NAME=${COUCHBASE_SCOPE_NAME:-$DEFAULT_SCOPE_NAME}
    USERNAME=${COUCHBASE_USERNAME:-admin}
    PASSWORD=${COUCHBASE_PASSWORD:-couchbase}
fi

echo "📝 Configuration:"
echo "   Bucket Name: $BUCKET_NAME"
echo "   Scope Name: $SCOPE_NAME"
echo ""

# Create output directory
OUTPUT_DIR="schema_generated"
TEMPLATE_DIR="schema"

# Clean and create output directory
if [ -d "$OUTPUT_DIR" ]; then
    rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}📁 Processing schema files...${NC}"

# Process each schema file
for file in "$TEMPLATE_DIR"/*.sql "$TEMPLATE_DIR"/v*; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        echo "   Processing: $filename"
        
        # Replace environment variable placeholders with actual values
        sed -e "s/\${COUCHBASE_BUCKET_NAME}/$BUCKET_NAME/g" \
            -e "s/\${COUCHBASE_SCOPE_NAME}/$SCOPE_NAME/g" \
            "$file" > "$OUTPUT_DIR/$filename"
    fi
done

# Generate setup.md with environment-specific values
echo "   Generating: setup.md"
cat > "$OUTPUT_DIR/setup.md" << EOF
# Database Setup Guide

Execute the schema files in the following order:

## Prerequisites
確保 Couchbase 容器已經啟動：
\`\`\`bash
docker ps | grep couchbase-ai
\`\`\`

## Setup Steps

### 1. 產生對應 .env 設定的 Schema 檔案
\`\`\`bash
# 從專案根目錄執行
bash generate_schema.sh
\`\`\`

### 2. 複製產生的 Schema 檔案到容器
\`\`\`bash
# 從專案根目錄執行
docker cp schema_generated/. couchbase-ai:/tmp/schema/
\`\`\`

### 3. 執行初始化腳本

#### 方法一：使用 Docker Exec + cbq

**重要：Bucket 必須透過 REST API 建立，無法用 SQL 語句建立**

\`\`\`bash
# 0. 建立 Bucket (使用 REST API)
curl -u $USERNAME:$PASSWORD -X POST http://localhost:8091/pools/default/buckets \\
  -d name=$BUCKET_NAME \\
  -d bucketType=couchbase \\
  -d ramQuotaMB=512 \\
  -d authType=sasl

# 等待 bucket 建立完成
sleep 5

# 0.1 建立 Scope
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD \\
  -s "CREATE SCOPE \\\`$BUCKET_NAME\\\`.\\\`$SCOPE_NAME\\\` IF NOT EXISTS;"

# 1. Initial Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/v0.0_init

# 2. User & RBAC Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/v0.1_user_rbac
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/v0.1_user_rbac_test_data

# 3. Team Roles Setup
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/v0.2_team_role

# 4. System Roles Initialization
docker exec couchbase-ai cbq -e "couchbase://localhost" -u $USERNAME -p $PASSWORD -f /tmp/schema/v0.3_system_role_init
\`\`\`

#### 方法二：使用 Couchbase Query Workbench
1. 訪問 Couchbase Web Console: http://localhost:8091
2. 使用帳密登入：\`$USERNAME\` / \`$PASSWORD\`
3. 進入 Query Workbench
4. 依序複製每個 \`schema_generated/\` 資料夾中的檔案內容並執行：
   - 手動建立 Bucket (透過 Web UI: Buckets → Add Bucket → 名稱: $BUCKET_NAME, 記憶體: 512MB)
   - 建立 Scope (透過 Query: \`CREATE SCOPE \\\`$BUCKET_NAME\\\`.\\\`$SCOPE_NAME\\\` IF NOT EXISTS;\`)
   - \`v0.0_init\` (建立 Collections)
   - \`v0.1_user_rbac\` (使用者權限系統)
   - \`v0.1_user_rbac_test_data\` (測試資料)
   - \`v0.2_team_role\` (團隊角色)
   - \`v0.3_system_role_init\` (系統角色)

## Collections Created

### Core Application
- \`document\` - File documents and metadata
- \`dataset\` - Data collections with access control
- \`chat-topic\` - Chat conversation topics
- \`chat-record\` - Individual chat interactions
- \`chat-message\` - Chat messages storage
- \`document-vector\` - Vector embeddings for AI

### Authentication & Authorization
- \`user\` - User accounts
- \`team\` - Teams/organizations
- \`team-membership\` - User-team relationships
- \`team-role\` - Team-specific roles
- \`system-role\` - System-wide roles

### System
- \`cache\` - AI and application caching

## Default Users Created

| Username | Email | Password | System Role |
|----------|-------|----------|-------------|
| super_admin | super_admin@example.com | password123 | SUPER_ADMIN |
| user_admin | user_admin@example.com | password123 | USER_ADMIN |
| team_admin | team_admin@example.com | password123 | TEAM_ADMIN |

## Verification

After setup, verify collections exist:
\`\`\`sql
SELECT name FROM system:keyspaces 
WHERE bucket_name = '$BUCKET_NAME' AND scope_name = '$SCOPE_NAME';
\`\`\`

Verify users exist:
\`\`\`sql
SELECT name, email, systemRoles FROM \`$BUCKET_NAME\`.\`$SCOPE_NAME\`.\`user\`;
\`\`\`
EOF

echo ""
echo -e "${GREEN}✅ Schema generation completed!${NC}"
echo "📂 Generated files in: $OUTPUT_DIR/"
echo ""
echo -e "${YELLOW}📋 Next steps:${NC}"
echo "1. Review generated files in $OUTPUT_DIR/"
echo "2. Copy schema files to Couchbase container:"
echo "   docker cp $OUTPUT_DIR/. couchbase-ai:/tmp/schema/"
echo "3. Execute initialization scripts in order"
echo ""
echo -e "${GREEN}🎉 Ready to initialize your database!${NC}"