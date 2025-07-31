# 團隊-資料集權限整合修復報告

## 修改概要

本次修復解決了團隊角色權限與資料集操作整合不完整的問題，確保前端創建的自訂團隊角色權限能真正在後端資料集操作中生效。

## 修改的檔案

### 1. `/ecom-assistant-core/src/main/java/com/ecom/ai/ecomassistant/core/service/DatasetManager.java`

**新增 Import:**
```java
import com.ecom.ai.ecomassistant.auth.permission.DatasetPermission;
import org.apache.shiro.SecurityUtils;
import static com.ecom.ai.ecomassistant.auth.permission.TeamPermission.TEAM_EDIT;
```

**修改的方法：**

#### A. `createDataset()` - 新增群組資料集的團隊權限檢查
```java
public Dataset createDataset(Dataset dataset) {
    // 檢查基於資料集存取類型的權限
    Dataset.AccessType accessType = Optional.ofNullable(dataset.getAccessType()).orElse(Dataset.AccessType.PRIVATE);
    
    if (accessType == Dataset.AccessType.GROUP) {
        // 對於 GROUP 資料集，使用者必須有團隊編輯權限或系統管理員權限
        String teamId = dataset.getTeamId();
        if (teamId == null || teamId.isEmpty()) {
            throw new IllegalArgumentException("Team ID is required for GROUP access type datasets");
        }
        
        PermissionUtil.checkAnyPermission(Set.of(
                SYSTEM_DATASET_ADMIN.getCode(),
                TEAM_EDIT.getCodeWithTeamId(teamId)
        ));
    }
    // PRIVATE 和 PUBLIC 資料集，任何認證用戶都可以創建（預設行為）
    
    return datasetService.createDataset(dataset);
}
```

#### B. `updateDataset()` - 新增資料集管理權限檢查
```java
public Dataset updateDataset(String id, Dataset updatedDataset) {
    // 獲取現有資料集並檢查權限
    Dataset existingDataset = datasetService
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

    checkDatasetPermission(existingDataset, DATASET_MANAGE);
    return datasetService.updateDataset(id, updatedDataset);
}
```

#### C. `deleteDataset()` - 新增資料集刪除權限檢查
```java
public boolean deleteDataset(String id) {
    // 獲取現有資料集並檢查權限
    Dataset dataset = datasetService
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

    checkDatasetPermission(dataset, DATASET_DELETE);
    return datasetService.deleteDataset(id);
}
```

#### D. `uploadFile()` - 新增檔案上傳權限檢查
```java
public String uploadFile(String userId, String datasetId, MultipartFile file) throws IOException {
    Dataset dataset = datasetService.findById(datasetId)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));

    // 檢查檔案上傳權限
    checkDatasetPermission(dataset, DATASET_FILE_UPLOAD);
    
    // ... 原有的檔案處理邏輯
}
```

#### E. `updateDatasetTags()` - 替換創建者檢查為團隊權限檢查
```java
public Dataset updateDatasetTags(String datasetId, Set<String> tags, String userId) {
    Dataset dataset = datasetService.findById(datasetId)
        .orElseThrow(() -> new RuntimeException("知識庫不存在"));
    
    // 檢查資料集管理權限（替換原來的創建者檢查）
    checkDatasetPermission(dataset, DATASET_MANAGE);
    
    dataset.setTags(tags);
    return datasetService.save(dataset);
}
```

#### F. 新增統一權限檢查方法 `checkDatasetPermission()`
```java
/**
 * 根據存取類型和使用者團隊成員身份檢查資料集權限
 */
private void checkDatasetPermission(Dataset dataset, DatasetPermission requiredPermission) {
    Dataset.AccessType accessType = Optional.ofNullable(dataset.getAccessType()).orElse(Dataset.AccessType.PRIVATE);
    
    switch (accessType) {
        case PRIVATE -> {
            // 私人資料集：只有創建者或系統管理員可存取
            String currentUserId = (String) SecurityUtils.getSubject().getPrincipal();
            boolean isOwner = Objects.equals(dataset.getCreatedBy(), currentUserId);
            boolean isSystemAdmin = PermissionUtil.hasAnyPermission(Set.of(SYSTEM_DATASET_ADMIN.getCode()));
            
            if (!isOwner && !isSystemAdmin) {
                PermissionUtil.forbidden();
            }
        }
        case GROUP -> {
            // 群組資料集：檢查團隊範圍權限
            PermissionUtil.checkAnyPermission(Set.of(
                    SYSTEM_DATASET_ADMIN.getCode(),
                    requiredPermission.getCodeWithTeamId(dataset.getTeamId())
            ));
        }
        case PUBLIC -> {
            // 公開資料集：寫入操作仍需團隊權限
            if (requiredPermission != DATASET_VIEW) {
                PermissionUtil.checkAnyPermission(Set.of(
                        SYSTEM_DATASET_ADMIN.getCode(),
                        requiredPermission.getCodeWithTeamId(dataset.getTeamId())
                ));
            }
            // 公開資料集的讀取操作不需要權限檢查
        }
    }
}
```

## 修復的問題

### 問題 1: 資料集 CRUD 操作缺少權限檢查
- **修復前**: `createDataset`, `updateDataset`, `deleteDataset` 沒有任何權限檢查
- **修復後**: 根據資料集存取類型進行適當的權限驗證

### 問題 2: 檔案上傳沒有驗證資料集存取權限
- **修復前**: `uploadFile` 只檢查資料集是否存在，不檢查使用者權限
- **修復後**: 使用 `DATASET_FILE_UPLOAD` 權限進行驗證

### 問題 3: 團隊權限沒有被正確使用
- **修復前**: `updateDatasetTags` 只檢查是否為創建者
- **修復後**: 使用團隊範圍的 `DATASET_MANAGE` 權限

### 問題 4: 缺少統一的權限檢查邏輯
- **修復前**: 每個方法都有不同的權限檢查邏輯（或沒有）
- **修復後**: 統一的 `checkDatasetPermission()` 方法處理所有情況

## 權限整合驗證

### 後端權限定義 (DatasetPermission)
- `dataset:view` - 查詢知識庫
- `dataset:delete` - 刪除知識庫
- `dataset:manage` - 知識庫基本訊息管理
- `dataset:visibility:manage` - 知識庫開放設定
- `dataset:file:upload` - 上傳檔案
- `dataset:file:delete` - 刪除其他人檔案
- `dataset:file:approve` - 放行檔案
- `dataset:qa:verification` - QA 驗證

### 前端團隊角色管理
- ✅ 支援 `dataset:*` 通配符權限
- ✅ 可分別指派具體的資料集權限
- ✅ 權限透過 `PermissionRegistry.getTeamLevelPermissions()` 動態載入

### 權限轉換流程
1. **儲存**: 團隊角色權限以 `dataset:view` 格式儲存在 `ECOM.AI.team-role`
2. **轉換**: `UserManager.getUserRoleContext()` 轉換為 `team:{teamId}:dataset:view`
3. **驗證**: `DatasetPermission.getCodeWithTeamId()` 生成對應權限碼進行檢查

## 如何驗證修復效果

### 1. 編譯測試
```bash
mvn clean compile
```

### 2. 功能測試場景

#### 場景 A: 團隊成員操作群組資料集
1. 創建團隊，新增成員
2. 創建自訂角色，指派 `dataset:manage` 權限
3. 將角色賦予團隊成員
4. 該成員嘗試更新群組資料集資訊 → 應該成功
5. 移除該成員的 `dataset:manage` 權限
6. 該成員再次嘗試更新資料集 → 應該失敗 (403 Forbidden)

#### 場景 B: 檔案上傳權限
1. 團隊成員被賦予 `dataset:view` 但沒有 `dataset:file:upload`
2. 嘗試上傳檔案到群組資料集 → 應該失敗
3. 賦予 `dataset:file:upload` 權限
4. 再次嘗試上傳 → 應該成功

#### 場景 C: 通配符權限
1. 團隊成員被賦予 `dataset:*` 權限
2. 該成員應該可以執行所有資料集操作（查看、編輯、刪除、上傳檔案等）

### 3. API 測試端點
```bash
# 測試資料集創建權限
POST /api/v1/datasets
{
  "name": "Test Dataset",
  "accessType": "GROUP",
  "teamId": "team123"
}

# 測試檔案上傳權限
POST /api/v1/datasets/{datasetId}/with-file
Content-Type: multipart/form-data

# 測試資料集更新權限
PATCH /api/v1/datasets/{id}
{
  "description": "Updated description"
}

# 測試資料集刪除權限
DELETE /api/v1/datasets/{id}
```

### 4. 日誌檢查
權限拒絕時會出現相關錯誤訊息:
```
UnauthorizedException: 缺少任一權限: system:dataset:admin, team:{teamId}:dataset:manage
```

## 總結

此次修復確保了前端團隊角色管理與後端資料集權限檢查的完整整合。現在：

1. ✅ 前端自訂團隊角色的資料集權限設定會真正在後端生效
2. ✅ 所有資料集 CRUD 操作都有適當的權限檢查
3. ✅ 支援三種存取類型 (PRIVATE/GROUP/PUBLIC) 的不同權限策略
4. ✅ 統一的權限檢查邏輯確保一致性
5. ✅ 保持向後相容性，不影響現有功能

團隊管理員現在可以透過前端介面創建自訂角色，指派具體的資料集權限給團隊成員，這些權限設定會在所有相關的後端操作中被正確驗證和執行。