# Q/A 功能故障排除指南

## 問題診斷

### 1. Q/A 沒有生成的可能原因

#### 已修復 ✅
- **文件上傳流程**: 已更新 `AiFileEventListener` 使用 `processFileWithQA()` 方法
- **錯誤處理**: 添加了回退機制，Q/A 生成失敗時仍會進行正常文檔處理

#### 需要檢查的配置 ⚠️

**OpenAI API 配置**:
```yaml
# application.yaml 中的配置
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # 需要設置環境變量
      chat:
        options:
          model: gpt-4.1-mini     # 使用的 OpenAI 模型
```

**環境變量檢查**:
```bash
# 檢查 OpenAI API Key 是否設置
echo $OPENAI_API_KEY

# 如果沒有設置，請添加到 .env 文件或環境變量
export OPENAI_API_KEY="your-openai-api-key-here"
```

**數據庫集合確認**:
```sql
-- 確認 qa 集合已創建
SELECT * FROM system:keyspaces WHERE name = 'qa';
```

### 2. 測試 Q/A 功能

#### 手動測試步驟:
1. 確保 `OPENAI_API_KEY` 環境變量已設置
2. 重新啟動應用: `mvn spring-boot:run -pl ecom-assistant-api`
3. 上傳新的 PDF 文件到任何數據集
4. 檢查應用日誌是否顯示 Q/A 生成過程
5. 查詢 Couchbase qa 集合確認數據

#### 日誌檢查:
應該看到類似的日誌訊息:
```
Processing file upload with Q/A generation: filename.pdf (dataset: xxx, document: xxx)
Generating Q/A pairs for document: filename.pdf (content length: xxx)
Successfully parsed X Q/A pairs
Successfully processed file filename.pdf with X documents and X Q/A pairs
```

#### 數據庫查詢:
```sql
-- 查看所有 Q/A 記錄
SELECT * FROM `Embedding`.`Testing`.`qa` LIMIT 10;

-- 按數據集查詢
SELECT * FROM `Embedding`.`Testing`.`qa` 
WHERE datasetId = 'your-dataset-id' 
ORDER BY questionIndex;
```

### 3. 常見問題解決

#### OpenAI API 錯誤:
- **401 Unauthorized**: 檢查 API Key 是否正確
- **429 Rate Limit**: OpenAI API 達到使用限制
- **模型錯誤**: 檢查 `gpt-4.1-mini` 模型是否可用

#### 數據庫問題:
- 確認 qa 集合已創建: `CREATE COLLECTION \`Embedding\`.\`Testing\`.\`qa\` IF NOT EXISTS;`
- 檢查 Couchbase 連接配置
- 確認用戶有寫入權限

#### 應用配置:
- 重新構建項目: `mvn clean install`
- 檢查 Spring 容器是否正確注入所有依賴
- 確認 @Service 和 @Component 註解正確

### 4. 手動 API 測試

如果自動處理失敗，可以通過程式碼直接測試:

```java
@Autowired
private EtlService etlService;

// 手動測試 Q/A 生成
FileInfo testFile = // 你的文件信息
List<QAPair> result = etlService.processFileWithQA(
    testFile, 
    "test-dataset-id", 
    "Test Dataset", 
    "test-document-id"
);
```

### 5. 下一步行動

1. **立即檢查**: 設置 `OPENAI_API_KEY` 環境變量
2. **重啟應用**: 確保新配置生效
3. **測試上傳**: 上傳新的 PDF 文件
4. **檢查日誌**: 觀察 Q/A 生成過程
5. **查詢數據**: 確認 qa 集合中有數據

### 6. 問題修復狀態

#### ✅ 已修復的問題 (2025-07-27)
1. **Couchbase查詢語法錯誤**: 
   - 修復了 `#{#n1ql.deleteEntity}` 語法錯誤
   - 改用標準N1QL查詢語法
   - 暫時簡化為直接保存，避免刪除操作錯誤

2. **Q/A生成確認正常**:
   - OpenAI API正常運作 (使用gpt-4.1-mini)
   - 成功生成10個Q/A對
   - 文檔內容解析正確

#### 🔄 測試結果
從日誌可以看到：
```
Successfully parsed 10 Q/A pairs
```
這確認了Q/A生成功能完全正常，問題僅在於數據庫保存。

#### 🔧 第二輪修復 (2025-07-27 - 第二次)

**問題1：Q/A生成邏輯錯誤**
- **原因**: 對每個文檔頁面分別生成10個Q/A（9頁 × 10個 = 90個Q/A）
- **修復**: 修改邏輯為對整份文檔生成10個Q/A
- **變更**: `generateQAPairsForEntireDocument()` 方法合併所有頁面內容

**問題2：Spring Security上下文錯誤**
- **原因**: `JwtAuditorAware`在異步處理中無法訪問SecurityManager
- **修復**: QAPair不再繼承AuditableDocument，改用簡單的時間戳記錄
- **變更**: 移除Spring Data Auditing依賴，手動設置createdAt/updatedAt

#### 🔧 下一步測試
1. 重新啟動應用測試修復後的版本
2. 應該看到：
   - 只生成10個Q/A而不是90個
   - 成功保存到qa集合
   - 日誌顯示："Generating 10 Q/A pairs for entire document"
3. 檢查 `qa` 集合是否有數據

### 7. 回滾選項

如果 Q/A 功能影響正常使用，可以暫時禁用:
```java
// 在 AiFileEventListener 中註釋掉 Q/A 處理，使用原始流程
List<Document> documents = etlService.processFile(fileInfo);
datasetInfoEnricher.transform(documents);
etlService.save(documents);
```