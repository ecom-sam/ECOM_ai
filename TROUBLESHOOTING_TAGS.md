# Dataset Tags 功能故障排除指南

## 問題：前端顯示「載入知識庫失敗」

### 可能原因與解決方案

#### 1. 資料庫遷移未執行
**問题：** 現有的 Dataset 資料沒有 `tags` 欄位，導致 API 回傳時 JSON 序列化失敗

**解決方案：**
```bash
# 1. 複製 schema 檔案到 Couchbase 容器
docker cp schema/05_migrate_dataset_tags.sql couchbase-ai:/tmp/

# 2. 執行資料庫遷移
docker exec -it couchbase-ai cbq -u Administrator -p password -s="$(cat /tmp/05_migrate_dataset_tags.sql)"
```

#### 2. 後端應用需要重啟
**問题：** 新的代碼變更（Dataset.tags 欄位、API 端點等）未生效

**解決方案：**
```bash
# 重新編譯並啟動後端
mvn clean compile -DskipTests
mvn spring-boot:run -pl ecom-assistant-api
```

#### 3. API 端點驗證
**驗證後端 API 是否正常：**
```bash
# 測試 API 端點（需要先登入獲取 token）
curl -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     http://localhost:8080/api/v1/datasets/for-chat
```

#### 4. 前端除錯
**開啟瀏覽器開發工具檢查：**
1. Network 標籤頁查看 API 請求狀態
2. Console 標籤頁查看錯誤訊息
3. 確認 JWT token 是否存在且有效

### 測試步驟

1. **確認資料庫遷移成功：**
   ```sql
   SELECT name, tags FROM `ECOM`.`AI`.`dataset` 
   WHERE _class = "com.ecom.ai.ecomassistant.db.model.Dataset" 
   LIMIT 5;
   ```

2. **確認後端 API 回應：**
   - API 端點：`GET /api/v1/datasets/for-chat`
   - 應該回傳包含 `tags` 欄位的 JSON 陣列

3. **確認前端功能：**
   - 打開 AI 對話頁面
   - 點擊右側知識庫按鈕
   - 應該能看到知識庫列表和 tag 篩選選項

### 常見錯誤訊息

- **"載入知識庫失敗"** → 通常是 API 調用失敗，檢查網路和認證
- **JSON parse error** → 後端資料結構問題，確認資料庫遷移
- **401 Unauthorized** → JWT token 過期或無效
- **500 Internal Server Error** → 後端代碼問題，檢查應用日誌

### 完整重建流程

如果問題持續存在，執行完整重建：

```bash
# 1. 停止後端應用
pkill -f "spring-boot:run"

# 2. 執行資料庫遷移
docker cp schema/05_migrate_dataset_tags.sql couchbase-ai:/tmp/
docker exec -it couchbase-ai cbq -u Administrator -p password -s="$(cat /tmp/05_migrate_dataset_tags.sql)"

# 3. 重新編譯後端
mvn clean compile -DskipTests

# 4. 啟動後端
mvn spring-boot:run -pl ecom-assistant-api &

# 5. 等待啟動完成後測試前端功能
```