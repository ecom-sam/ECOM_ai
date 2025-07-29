# QA驗證前端功能實作計畫

## 功能概述
在知識庫頁面中新增QA驗證功能，允許使用者對每個檔案產生的QA進行審核，並支援批量向量化處理。

## 使用者流程
1. 進入知識庫詳細頁面，查看檔案列表
2. 點擊檔案旁的"QA驗證"按鈕
3. 彈出QA驗證視窗，顯示該檔案的所有QA
4. 逐一審核每個QA，選擇通過/拒絕
5. 支援隨時修改QA狀態（通過↔拒絕）
6. 點擊"提交"按鈕，批量處理所有QA狀態變更
7. 系統自動對通過的QA進行向量化，拒絕的QA從向量庫中移除

## 前端實作規劃

### 1. 檔案列表UI更新
**檔案**: `/src/pages/KnowledgeBase/KnowledgeBaseDetail.tsx`

**修改內容**:
- 在每個檔案項目旁新增"QA驗證"按鈕
- 顯示QA統計資訊（待驗證/已通過/已拒絕數量）
- 按鈕狀態根據QA驗證狀態變色

```tsx
// 新增QA統計顯示
<div className="file-qa-stats">
  <Tag color="orange">待驗證: {file.pendingQACount}</Tag>
  <Tag color="green">已通過: {file.approvedQACount}</Tag>
  <Tag color="red">已拒絕: {file.rejectedQACount}</Tag>
</div>

// QA驗證按鈕
<Button 
  type="primary" 
  size="small"
  onClick={() => openQAVerificationModal(file.id)}
>
  QA驗證 ({file.totalQACount})
</Button>
```

### 2. QA驗證Modal元件
**新檔案**: `/src/pages/KnowledgeBase/components/QAVerificationModal.tsx`

**功能特點**:
- 分頁顯示QA列表（處理大量QA的情況）
- 每個QA項目包含：問題、答案、當前狀態、操作按鈕
- 支援批量選擇操作（全選通過/全選拒絕）
- 狀態變更即時反映在UI上
- 提交前確認對話框

**主要狀態管理**:
```tsx
interface QAItem {
  id: string;
  question: string;
  answer: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  originalStatus: 'PENDING' | 'APPROVED' | 'REJECTED'; // 追蹤原始狀態
  isModified: boolean; // 是否有變更
}

const [qaList, setQAList] = useState<QAItem[]>([]);
const [modifiedQAs, setModifiedQAs] = useState<Set<string>>(new Set());
```

### 3. QA列表項目元件
**新檔案**: `/src/pages/KnowledgeBase/components/QAListItem.tsx`

**UI設計**:
```tsx
<Card className="qa-item" size="small">
  <div className="qa-header">
    <Text strong>Q: {item.question}</Text>
    <Tag color={getStatusColor(item.status)}>{getStatusText(item.status)}</Tag>
  </div>
  
  <div className="qa-answer">
    <Text>A: {item.answer}</Text>
  </div>
  
  <div className="qa-actions">
    <Radio.Group 
      value={item.status} 
      onChange={(e) => onStatusChange(item.id, e.target.value)}
    >
      <Radio.Button value="APPROVED" className="approve-btn">
        ✓ 通過
      </Radio.Button>
      <Radio.Button value="REJECTED" className="reject-btn">
        ✗ 拒絕
      </Radio.Button>
    </Radio.Group>
    
    {item.isModified && (
      <Tag color="blue" size="small">已修改</Tag>
    )}
  </div>
</Card>
```

### 4. 批量操作工具列
**位置**: QAVerificationModal頂部

**功能**:
- 全選/取消全選
- 批量通過/批量拒絕選中項目
- 顯示修改統計（X個待提交）
- 重置所有修改

```tsx
<div className="batch-operations">
  <Checkbox 
    checked={allSelected}
    onChange={onSelectAll}
  >
    全選 ({selectedQAs.size}/{qaList.length})
  </Checkbox>
  
  <Button.Group>
    <Button 
      type="primary" 
      size="small"
      onClick={() => batchUpdateStatus('APPROVED')}
      disabled={selectedQAs.size === 0}
    >
      批量通過
    </Button>
    <Button 
      danger 
      size="small"
      onClick={() => batchUpdateStatus('REJECTED')}
      disabled={selectedQAs.size === 0}
    >
      批量拒絕
    </Button>
  </Button.Group>
  
  <div className="modification-summary">
    <Tag color="blue">待提交修改: {modifiedQAs.size}</Tag>
  </div>
</div>
```

### 5. API整合服務
**檔案**: `/src/utils/apiClient.tsx`

**新增API方法**:
```tsx
// 獲取檔案的QA列表
export const getFileQAs = (documentId: string) => {
  return apiClient.get(`/api/v1/qa/document/${documentId}`);
};

// 獲取檔案QA統計
export const getFileQAStats = (documentId: string) => {
  return apiClient.get(`/api/v1/qa/document/${documentId}/stats`);
};

// 批量更新QA狀態
export const batchUpdateQAStatus = (updates: QAStatusUpdate[]) => {
  return apiClient.post('/api/v1/qa/batch/update-status', { updates });
};

// 批量向量化已通過的QA
export const vectorizeApprovedQAs = (documentId: string) => {
  return apiClient.post(`/api/v1/qa/document/${documentId}/vectorize`);
};
```

### 6. 狀態管理與資料流
**狀態更新流程**:
1. 載入檔案QA列表 → `getFileQAs(documentId)`
2. 使用者修改QA狀態 → 更新本地狀態 + 標記為已修改
3. 提交批量更新 → `batchUpdateQAStatus(changes)`
4. 觸發向量化 → `vectorizeApprovedQAs(documentId)`
5. 重新載入QA統計 → 更新檔案列表顯示

**錯誤處理**:
- API呼叫失敗時顯示具體錯誤訊息
- 網路錯誤時提供重試機制
- 部分QA處理失敗時顯示詳細報告

### 7. 樣式設計
**新檔案**: `/src/pages/KnowledgeBase/components/QAVerification.css`

**設計重點**:
- QA項目卡片式佈局，清晰分隔
- 狀態顏色編碼（綠色=通過，紅色=拒絕，橙色=待審）
- 修改狀態視覺提示
- 響應式設計，支援行動裝置
- 載入狀態與骨架屏

## 後端API需求

### 1. QA查詢API
```
GET /api/v1/qa/document/{documentId}
GET /api/v1/qa/document/{documentId}/stats
```

### 2. QA狀態更新API
```
POST /api/v1/qa/batch/update-status
Body: {
  updates: [
    { qaId: string, status: 'APPROVED' | 'REJECTED', note?: string }
  ]
}
```

### 3. 向量化處理API
```
POST /api/v1/qa/document/{documentId}/vectorize
```

### 4. 向量刪除API（拒絕QA時）
```
DELETE /api/v1/qa/{qaId}/vector
```

## 實作優先順序

### Phase 1: 基礎UI (高優先級)
1. ✅ 檔案列表新增QA驗證按鈕
2. ✅ 建立QAVerificationModal基本架構
3. ✅ 實作QA列表顯示

### Phase 2: 狀態管理 (高優先級)
1. ✅ QA狀態切換邏輯
2. ✅ 修改狀態追蹤
3. ✅ 批量操作功能

### Phase 3: API整合 (高優先級)
1. ✅ 串接後端QA查詢API
2. ✅ 實作批量狀態更新
3. ✅ 向量化處理串接

### Phase 4: 優化與完善 (中優先級)
1. ✅ 錯誤處理與使用者體驗
2. ✅ 載入狀態與效能優化
3. ✅ 樣式完善與響應式設計

## 技術考量

### 效能優化
- QA列表分頁載入，避免一次載入過多資料
- 虛擬捲動處理大量QA項目
- 狀態變更防抖，避免頻繁API呼叫

### 使用者體驗
- 操作確認對話框，防止誤操作
- 進度指示器顯示批量處理進度
- 快捷鍵支援（Ctrl+A全選等）

### 資料一致性
- 本地狀態與後端同步檢查
- 並行修改衝突處理
- 離線狀態處理與同步

## 測試策略

### 單元測試
- QA狀態切換邏輯測試
- 批量操作功能測試
- API呼叫Mock測試

### 整合測試
- 完整的QA驗證流程測試
- 向量化處理驗證
- 錯誤情境處理測試

### 使用者測試
- 大量QA處理效能測試
- 多使用者並行操作測試
- 各種裝置響應式測試

## 預期效果
1. **效率提升**: 批量QA審核取代逐一處理
2. **品質保證**: 明確的通過/拒絕機制確保QA品質
3. **靈活性**: 支援隨時修改QA狀態
4. **自動化**: 審核後自動進行向量化處理
5. **可視化**: 清晰的統計資訊與狀態顯示

此實作計畫涵蓋了從UI設計到API整合的完整開發流程，確保QA驗證功能能夠順利整合至現有系統中。