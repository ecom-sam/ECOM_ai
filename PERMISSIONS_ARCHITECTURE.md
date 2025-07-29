# 🔐 權限架構與角色配置文檔

## 📋 目錄
- [架構概覽](#架構概覽)
- [權限層級](#權限層級)
- [系統角色](#系統角色)
- [團隊角色](#團隊角色)
- [權限分類](#權限分類)
- [預設測試帳號](#預設測試帳號)
- [QA 驗證權限](#qa-驗證權限)
- [權限檢查機制](#權限檢查機制)
- [前端權限控制](#前端權限控制)

---

## 🏗️ 架構概覽

本系統採用**三層權限架構**：
```
系統級權限 (System Level)
    ↓
團隊級權限 (Team Level)
    ↓
資源級權限 (Resource Level)
```

### 權限格式
- **系統級**: `system:*`, `system:user:manage`
- **團隊級**: `team:{teamId}:dataset:*`, `team:{teamId}:team:view`
- **資源級**: 特定功能權限，如 `qa.verification`

---

## 🎯 權限層級

### 1. 系統級權限 (SystemPermission)
適用於整個系統的全域權限

| 權限代碼 | 權限名稱 | 描述 |
|---------|---------|-----|
| `system:*` | 系統超級管理權限 | 系統最高權限 |
| `system:team:*` | 團隊管理全部權限 | 所有團隊管理功能 |
| `system:team:view` | 檢視團隊 | 查看團隊資訊 |
| `system:team:manage` | 管理團隊 | 創建、編輯團隊 |
| `system:team:grant-admin` | 授權團隊管理員 | 指派團隊管理員 |
| `system:user:*` | 使用者全部權限 | 所有使用者管理功能 |
| `system:user:list` | 使用者清單 | 查看使用者列表 |
| `system:user:view` | 檢視使用者 | 查看使用者詳情 |
| `system:user:invite` | 邀請使用者 | 邀請新使用者 |
| `system:user:manage` | 管理使用者 | 管理使用者帳號 |
| `system:dataset:*` | 知識庫管理員 | 所有知識庫權限 |

### 2. 團隊級權限 (TeamPermission)
適用於特定團隊內的權限

| 權限代碼 | 權限名稱 | 描述 |
|---------|---------|-----|
| `team:view` | 檢視團隊資訊 | 查看團隊基本資訊 |
| `team:edit` | 編輯團隊 | 修改團隊設定 |
| `members:view` | 查看團隊成員 | 檢視成員列表 |
| `members:invite` | 邀請團隊成員 | 邀請新成員加入 |
| `members:manage` | 管理團隊成員 | 管理成員權限 |
| `roles:view` | 查看團隊角色 | 檢視角色設定 |
| `roles:manage` | 管理團隊角色 | 創建、編輯角色 |

### 3. 知識庫權限 (DatasetPermission)
適用於知識庫資源的權限

| 權限代碼 | 權限名稱 | 描述 |
|---------|---------|-----|
| `dataset:view` | 查詢知識庫 | 查看知識庫內容 |
| `dataset:delete` | 刪除知識庫 | 刪除知識庫 |
| `dataset:manage` | 知識庫基本訊息管理 | 管理知識庫設定 |
| `dataset:visibility:manage` | 知識庫開放設定 | 管理可見性設定 |
| `dataset:file:upload` | 上傳檔案 | 上傳文檔到知識庫 |
| `dataset:file:delete` | 刪除其他人檔案 | 刪除他人上傳的檔案 |
| `dataset:file:approve` | 放行檔案 | 審核並放行檔案 |
| `dataset:qa:verification` | QA 驗證 | AI 生成問答對的審核權限 |

---

## 👑 系統角色

### SUPER_ADMIN (系統超級管理員)
- **權限**: `system:*`
- **描述**: 擁有系統所有權限的超級管理員
- **能力**: 所有系統功能，包括用戶管理、團隊管理、知識庫管理等

### USER_ADMIN (使用者管理員)
- **權限**: 
  - `system:user:manage`
  - `system:user:view`
  - `system:user:create`
  - `system:user:update`
  - `system:user:delete`
  - `system:user:activate`
  - `system:user:deactivate`
- **描述**: 專門管理使用者帳號的管理員

### TEAM_ADMIN (團隊管理員)
- **權限**:
  - `system:team:view`
  - `system:team:create`
  - `system:team:update`
  - `system:team:manage`
  - `team:*`
- **描述**: 管理團隊的管理員

### REGULAR_USER (一般使用者)
- **權限**:
  - `system:user:view:self`
  - `system:user:update:self`
- **描述**: 系統的一般使用者，只能管理自己的帳號

---

## 👥 團隊角色

### team-admin (團隊管理者)
- **權限**: `team:*`, `dataset:*`
- **描述**: 團隊內的最高權限角色
- **能力**: 管理團隊成員、角色，以及所有知識庫操作

### dataset-manager (知識庫管理者)
- **權限**: `dataset:*`
- **描述**: 可執行所有知識庫操作
- **能力**: 管理知識庫、上傳檔案、審核內容

### dataset-contributor (知識庫貢獻者)
- **權限**: `dataset:view`, `dataset:file:upload`
- **描述**: 知識庫內容貢獻者
- **能力**: 查看知識庫、上傳檔案

### team-member (團隊成員)
- **權限**: `team:view`, `dataset:view`
- **描述**: 一般團隊成員
- **能力**: 查看團隊資訊和知識庫內容

---

## 🎯 權限分類

### 系統管理類
- 使用者管理：創建、編輯、刪除使用者
- 團隊管理：創建、管理團隊
- 系統設定：系統級配置

### 團隊協作類
- 成員管理：邀請、管理團隊成員
- 角色管理：設定團隊內角色權限
- 團隊設定：團隊基本資訊管理

### 知識庫類
- 內容管理：上傳、編輯、刪除文檔
- 權限管理：設定知識庫存取權限
- 審核管理：審核上傳內容

---

## 🧪 預設測試帳號

| 使用者名稱 | 密碼 | 系統角色 | 描述 |
|-----------|------|---------|-----|
| `super_admin` | `super_admin` | SUPER_ADMIN | 系統超級管理員 |
| `user_admin` | `user_admin` | USER_ADMIN | 使用者管理員 |
| `team_admin` | `team_admin` | TEAM_ADMIN | 團隊管理員 |

### 測試團隊角色
所有測試帳號在預設團隊中都具有以下角色：
- 團隊管理者 (team-admin)
- 知識庫管理者 (dataset-manager)
- 知識庫貢獻者 (dataset-contributor)
- 團隊成員 (team-member)

---

## 🔍 QA 驗證權限系統

### 權限分級架構
QA 驗證採用三層權限控制：

```
系統級權限 (最高優先級)
├── system:SUPER_ADMIN → 完整 QA 驗證權限
└── system:TEAM_ADMIN → 完整 QA 驗證權限

資源級權限 (特定功能)
└── dataset:qa:verification → QA 驗證專門權限
```

### 權限檢查邏輯
QA 驗證功能的權限檢查實作：

```java
private boolean hasQAVerificationPermission(String userId) {
    // 1. 檢查系統管理員角色（最高優先級）
    Set<String> allowedRoles = Set.of("system:SUPER_ADMIN", "system:TEAM_ADMIN");
    boolean hasAdminRole = userRoleContext.roles().stream()
            .anyMatch(allowedRoles::contains);
    
    if (hasAdminRole) {
        return true; // 系統管理員具有完整權限
    }
    
    // 2. 檢查專門的 QA 驗證權限
    boolean hasQAPermission = userRoleContext.permissions()
            .contains("dataset:qa:verification");
    return hasQAPermission;
}
```

### QA 驗證權限者
| 權限等級 | 角色/權限 | 權限範圍 | 說明 |
|---------|----------|---------|-----|
| **最高級** | `system:SUPER_ADMIN` | 全系統 QA 驗證 | 系統超級管理員，可審核所有 QA |
| **高級** | `system:TEAM_ADMIN` | 跨團隊 QA 驗證 | 團隊管理員，可管理多個團隊的 QA |
| **專門級** | `dataset:qa:verification` | 特定資源 QA 驗證 | QA 審核專員，針對授權的知識庫進行 QA 驗證 |

### QA 驗證完整功能
#### 📋 QA 管理功能
- **查詢 QA 列表**: 查看指定文檔的所有 AI 生成問答對
- **QA 統計資訊**: 獲取 pending/approved/rejected 的數量統計
- **批量狀態更新**: 同時審核多個 QA 的狀態

#### ✅ QA 審核工作流
1. **待審核 (PENDING)**: AI 生成的 QA 初始狀態
2. **通過 (APPROVED)**: 審核通過，觸發向量化處理
3. **拒絕 (REJECTED)**: 審核拒絕，清除相關向量資料

#### 🔄 向量化管理
- **自動向量化**: 通過審核的 QA 自動加入向量資料庫
- **向量刪除**: 拒絕的 QA 自動清除向量資料
- **向量狀態追蹤**: 記錄 QA 的向量化狀態

### QA 驗證 API 端點
| HTTP 方法 | 端點 | 功能 | 權限要求 |
|----------|------|------|---------|
| `GET` | `/api/v1/qa/document/{documentId}` | 取得文檔 QA 列表 | QA 驗證權限 |
| `GET` | `/api/v1/qa/document/{documentId}/stats` | 取得 QA 統計資訊 | QA 驗證權限 |
| `POST` | `/api/v1/qa/batch/update-status` | 批量更新 QA 狀態 | QA 驗證權限 |
| `POST` | `/api/v1/qa/document/{documentId}/vectorize` | 向量化已通過的 QA | QA 驗證權限 |
| `DELETE` | `/api/v1/qa/{qaId}/vector` | 刪除 QA 向量 | QA 驗證權限 |

### 前端權限整合
```typescript
// 前端權限檢查
const hasQAVerificationPermission = () => {
    return hasPermission(PERMISSIONS.SYSTEM.SUPER_ADMIN) ||
           hasPermission(PERMISSIONS.SYSTEM.TEAM_ADMIN) ||
           hasPermission(PERMISSIONS.DATASET.QA_VERIFICATION);
};

// 條件式 UI 渲染
<PermissionGate
  anyOf={[
    PERMISSIONS.SYSTEM.SUPER_ADMIN,
    PERMISSIONS.SYSTEM.TEAM_ADMIN,
    PERMISSIONS.DATASET.QA_VERIFICATION
  ]}
>
  <Button onClick={handleQAVerification}>
    QA驗證
  </Button>
</PermissionGate>
```

### QA 驗證最佳實踐
#### 🔒 安全考量
- **雙重權限檢查**: 前端 UI 控制 + 後端 API 驗證
- **詳細日誌記錄**: 記錄所有 QA 審核操作與權限檢查
- **異步處理保護**: QA 向量化在異步上下文中安全執行

#### 📊 效能最佳化
- **批量操作**: 支援同時審核多個 QA，減少 API 調用
- **並行處理**: 向量化與狀態更新並行執行
- **快取統計**: QA 統計資訊適度快取，提升載入速度

#### 🎯 使用者體驗
- **即時回饋**: 審核操作後立即更新 UI 狀態
- **錯誤處理**: 完整的錯誤提示與恢復機制
- **批量選擇**: 支援全選/反選，提升審核效率

---

## ⚙️ 權限檢查機制

### 後端權限檢查
1. **JWT Token 解析**: 從 Authorization Header 取得使用者身份
2. **使用者角色獲取**: 通過 `UserManager.getUserRoleContext()` 獲取角色與權限
3. **權限比對**: 檢查使用者是否具有所需權限
4. **通配符支援**: 支援 `*` 通配符權限（如 `system:*` 包含所有系統權限）

### 權限格式化
- **系統角色**: `system:ROLE_NAME`
- **團隊角色**: `team:{teamId}:ROLE_NAME`
- **具體權限**: 實際的權限字串

### 權限繼承
```
system:* 
  ├── system:user:*
  │   ├── system:user:view
  │   ├── system:user:manage
  │   └── system:user:delete
  └── system:team:*
      ├── system:team:view
      └── system:team:manage
```

---

## 🎨 前端權限控制

### 權限常數定義
```typescript
export const PERMISSIONS = {
    TEAM: {
        MANAGE: "system:team:manage"
    },
    DATASET: {
        QA_VERIFICATION: "dataset:qa:verification"
    },
    SYSTEM: {
        SUPER_ADMIN: "system:SUPER_ADMIN",
        TEAM_ADMIN: "system:TEAM_ADMIN"
    }
}
```

### 權限檢查組件
- **`PermissionGate`**: 條件式渲染組件
- **`RequireAuth`**: 路由層級的認證保護
- **`hasPermission()`**: 程式化權限檢查函數

### 使用範例
```typescript
// 條件式渲染
<PermissionGate
  anyOf={[
    PERMISSIONS.SYSTEM.SUPER_ADMIN,
    PERMISSIONS.SYSTEM.TEAM_ADMIN,
    PERMISSIONS.DATASET.QA_VERIFICATION
  ]}
>
  <Button onClick={handleQAVerification}>
    QA驗證
  </Button>
</PermissionGate>

// 程式化檢查
if (hasPermission(PERMISSIONS.SYSTEM.SUPER_ADMIN)) {
  // 執行管理員功能
}
```

---

## 🔧 權限配置最佳實踐

### 1. 最小權限原則
- 使用者只獲得執行其工作所需的最少權限
- 避免過度授權

### 2. 角色基礎存取控制 (RBAC)
- 通過角色來管理權限，而非直接指派個別權限
- 簡化權限管理複雜度

### 3. 權限分層設計
- 系統級 → 團隊級 → 資源級的清晰分層
- 支援權限繼承與覆蓋

### 4. 動態權限檢查
- 前後端都進行權限檢查
- 後端為最終權限控制點

### 5. 審計追蹤
- 記錄權限變更與存取日誌
- 支援安全稽核需求

---

## 📚 相關檔案位置

### 後端權限實作
- `SystemPermission.java` - 系統權限定義
- `TeamPermission.java` - 團隊權限定義  
- `DatasetPermission.java` - 知識庫權限定義
- `PermissionRegistry.java` - 權限註冊中心
- `PermissionUtil.java` - 權限檢查工具
- `QAController.java` - QA 驗證權限邏輯

### 前端權限實作
- `permission.ts` - 權限常數定義
- `AuthContext.tsx` - 認證上下文
- `PermissionGate.tsx` - 權限門控組件

### 資料庫初始化
- `03_data_system_roles.sql` - 系統角色初始資料
- `03_data_team_roles.sql` - 團隊角色初始資料
- `03_data_users.sql` - 測試使用者資料

---

*最後更新：2025-07-29*
*文檔版本：v1.0*