# 後端權限控管架構完整指南

## 概述

本系統採用 **Apache Shiro + JWT** 的權限控管架構，支援三層權限體系：
- **系統級權限** (system:*)
- **團隊級權限** (team:{teamId}:*)  
- **資源級權限** (具體操作權限)

---

## 權限架構組件

### 1. 權限定義層 (SystemPermission.java)

權限常數定義，每個權限包含：
- `code`: 權限字串 (如 "system:*")
- `group`: 權限群組 (如 "system")
- `label`: 顯示名稱
- `description`: 權限描述

```java
public enum SystemPermission implements Permission {
    SYSTEM_SUPER_ADMIN("system:*", "system", "系統超級管理權限"),
    SYSTEM_TEAM_ADMIN("system:team:*", "system", "團隊管理全部權限"),
    SYSTEM_USER_LIST("system:user:list", "system", "使用者清單"),
    SYSTEM_DATASET_ADMIN("system:dataset:*", "system", "知識庫管理員"),
    // ... 其他權限定義
}
```

### 2. 角色映射層 (SystemRole.java)

將權限組合成角色，每個角色包含：
- 角色名稱
- 權限集合
- 描述信息

```java
public enum SystemRole implements Role {
    SUPER_ADMIN("system super admin", "", Set.of(
        SystemPermission.SYSTEM_SUPER_ADMIN)        // → "system:*"
    ),
    
    TEAM_ADMIN("system team admin", "", Set.of(
        SystemPermission.SYSTEM_TEAM_ADMIN,         // → "system:team:*"
        SystemPermission.SYSTEM_USER_LIST           // → "system:user:list"
    )),
    
    USER("user", "", Set.of()),                     // → 空權限
    
    // ... 其他角色定義
}
```

### 3. 用戶權限生成 (UserManager.java)

動態生成用戶的完整權限清單：

```java
public UserRoleContext getUserRoleContext(User user) {
    Set<String> permissions = new HashSet<>();
    
    // 1. 系統角色權限
    for (String systemRoleName : user.getSystemRoles()) {
        SystemRole systemRole = SystemRole.fromName(systemRoleName);
        permissions.addAll(systemRole.getPermissionCodes());
    }
    
    // 2. 團隊擁有者權限
    for (TeamMembership membership : userTeamMemberships) {
        Team team = teamService.findById(membership.getTeamId());
        if (Objects.equals(team.getOwnerId(), user.getId())) {
            permissions.add("team:" + team.getId() + ":*");  // 萬用權限
        }
    }
    
    // 3. 團隊角色權限
    for (String teamRoleId : membership.getTeamRoles()) {
        TeamRole teamRole = teamRoleService.findById(teamRoleId);
        for (String permission : teamRole.getPermissions()) {
            permissions.add("team:" + team.getId() + ":" + permission);
        }
    }
    
    return new UserRoleContext(roles, permissions);
}
```

---

## 權限檢查機制

### 1. 註解式權限檢查 (@RequiresPermissions)

Apache Shiro 提供的宣告式權限控制：

```java
@RequiresPermissions({"system:user:list"})
public List<User> getAllUsers() {
    // Shiro 自動檢查權限，失敗時拋出 UnauthorizedException
}

@RequiresPermissions({"system:team:manage", "system:team:grant_admin"})  // AND 邏輯
public Team createTeam(@RequestBody TeamCreateRequest request) {
    // 需要同時擁有兩個權限
}
```

### 2. 程式化權限檢查 (PermissionUtil)

OR 邏輯的權限檢查：

```java
// 檢查是否擁有任一權限 (OR 邏輯)
PermissionUtil.checkAnyPermission(Set.of(
    "system:team:manage",                    // 系統管理員 OR
    "team:" + teamId + ":members:view"       // 團隊成員查看權限
));

// 單一權限檢查
boolean hasPermission = PermissionUtil.hasAnyPermission(Set.of("system:dataset:*"));
```

### 3. 業務邏輯權限檢查

在方法內部根據資料動態檢查：

```java
public Dataset getDataset(String datasetId, String userId) {
    Dataset dataset = datasetService.findById(datasetId);
    
    switch (dataset.getAccessType()) {
        case PRIVATE -> {
            if (!Objects.equals(dataset.getCreatedBy(), userId)) {
                throw new AccessDeniedException("無權限存取私人資料集");
            }
        }
        case GROUP -> PermissionUtil.checkAnyPermission(Set.of(
            "system:dataset:*",
            "team:" + dataset.getTeamId() + ":dataset:view"
        ));
        case PUBLIC -> {
            // 公開資料集，所有人都可存取
        }
    }
    
    return dataset;
}
```

---

## 權限繼承與萬用字元

### 萬用字元權限規則

Apache Shiro 支援萬用字元權限繼承：

```java
// 權限繼承層級
"system:*"                    // 最高級：所有系統權限
  ├── "system:team:*"         // 所有團隊相關權限
  │   ├── "system:team:view"
  │   ├── "system:team:manage"
  │   └── "system:team:create"
  ├── "system:user:*"         // 所有用戶相關權限
  │   ├── "system:user:list"
  │   ├── "system:user:invite"
  │   └── "system:user:manage"
  └── "system:dataset:*"      // 所有知識庫權限

"team:{teamId}:*"             // 特定團隊的所有權限
  ├── "team:{teamId}:view"
  ├── "team:{teamId}:edit"
  ├── "team:{teamId}:members:*"
  │   ├── "team:{teamId}:members:view"
  │   ├── "team:{teamId}:members:invite"
  │   └── "team:{teamId}:members:manage"
  └── "team:{teamId}:dataset:*"
      ├── "team:{teamId}:dataset:view"
      ├── "team:{teamId}:dataset:manage"
      └── "team:{teamId}:dataset:delete"
```

### 權限匹配邏輯

```java
// 示例：用戶擁有 "system:team:*" 權限
subject.isPermitted("system:team:view")     → true  ✅
subject.isPermitted("system:team:manage")   → true  ✅
subject.isPermitted("system:user:list")     → false ❌
subject.isPermitted("team:xxx:view")        → false ❌
```

---

## 實際用戶權限分析

### 預設用戶權限分配

| 用戶 | systemRoles | 系統權限 | 團隊權限 | 實際能力 |
|------|-------------|----------|----------|----------|
| **super_admin** | `["SUPER_ADMIN"]` | `"system:*"` | 團隊擁有者權限 | 所有功能 |
| **team_admin** | `["TEAM_ADMIN"]` | `"system:team:*"`, `"system:user:list"` | - | 團隊管理 + 用戶列表 |
| **user_admin** | `["USER_ADMIN"]` | `"system:user:*"` | - | 用戶管理 |
| **sam0219mm** | `["USER"]` | **無** | `"team:c79e8f7a...:*"` | 僅團隊相關功能 |

### sam0219mm 權限詳細分析

```json
{
  "systemRoles": ["USER"],                           // SystemRole.USER → 空權限
  "permissions": [
    "team:c79e8f7a-7d4d-47d7-982e-e87b69df5ab5:*"   // 團隊擁有者萬用權限
  ],
  "teamOwnership": {
    "teamId": "c79e8f7a-7d4d-47d7-982e-e87b69df5ab5",
    "teamName": "TEST",
    "isOwner": true
  }
}
```

**能夠存取的功能**：
- ✅ 團隊管理 (`team:{teamId}:*` 涵蓋)
- ✅ 知識庫管理 (`team:{teamId}:dataset:*` 涵蓋)
- ✅ 成員管理 (`team:{teamId}:members:*` 涵蓋)
- ❌ 聊天功能 (需要 `system:chat:access`)
- ❌ 用戶管理 (需要 `system:user:*`)

---

## API 權限控制模式

### 模式1：註解 + 擁有者檢查

```java
@RequiresPermissions({"system:chat:access"})
public Flux<String> sendMessage(@CurrentUserId String userId, @PathVariable String topicId) {
    // 1. Shiro 檢查系統權限
    // 2. 業務邏輯檢查擁有者
    Optional<ChatTopic> topic = chatTopicService.findById(topicId);
    if (!topic.get().getCreatedBy().equals(userId)) {
        return Flux.error(new RuntimeException("Access denied"));
    }
    
    return chatService.sendMessage(...);
}
```

### 模式2：OR 邏輯權限檢查

```java
public Team getTeam(@PathVariable String teamId) {
    PermissionUtil.checkAnyPermission(Set.of(
        "system:team:manage",        // 系統管理員 OR
        "team:" + teamId + ":view"   // 團隊成員
    ));
    
    return teamService.findById(teamId);
}
```

### 模式3：動態資料過濾

```java
public Page<Dataset> getDatasets(String userId, Pageable pageable) {
    boolean canViewAll = PermissionUtil.hasAnyPermission(Set.of("system:dataset:*"));
    
    if (canViewAll) {
        return datasetService.findAll(pageable);
    } else {
        // 根據用戶團隊關係動態過濾
        Set<String> userTeamIds = getUserTeamIds(userId);
        return datasetService.findVisibleDatasets(userId, userTeamIds, pageable);
    }
}
```

---

## 權限問題診斷

### 常見權限問題

1. **權限定義但未分配**
   ```java
   // SystemPermission.java 有定義
   SYSTEM_USER_VIEW("system:user:view", ...)
   
   // 但 SystemRole.java 沒有角色分配此權限 → 永遠無法使用
   ```

2. **API 需要但無權限**
   ```java
   @RequiresPermissions({"system:chat:access"})  // API 需要
   // 但 SystemPermission.java 沒有定義此權限 → 只有 system:* 能存取
   ```

3. **權限層級不匹配**
   ```java
   // 用戶有 team:{teamId}:* 權限
   // 但 API 檢查 system:chat:access → 無法存取
   ```

### 權限檢查工具

```java
// 除錯用的權限檢查方法
public class PermissionDebugUtil {
    public static void debugUserPermissions(String userId) {
        User user = userService.findById(userId);
        UserRoleContext context = userManager.getUserRoleContext(user);
        
        log.info("User {} permissions:", userId);
        context.permissions().forEach(perm -> log.info("  - {}", perm));
        
        // 測試特定權限
        boolean canChat = context.permissions().stream()
            .anyMatch(p -> p.equals("system:chat:access") || 
                          p.equals("system:*"));
        log.info("Can access chat: {}", canChat);
    }
}
```

---

## 最佳實踐建議

### 1. 權限設計原則

- **最小權限原則**: 用戶只獲得執行任務所需的最小權限
- **萬用字元優先**: 優先使用萬用字元權限 (`system:*`, `team:*`) 而不是細粒度權限
- **權限一致性**: 確保權限定義、角色分配、API 檢查三者一致

### 2. 權限實現模式

```java
// 推薦：使用 OR 邏輯，支援多級權限
PermissionUtil.checkAnyPermission(Set.of(
    "system:*",                          // 系統管理員
    "system:team:*",                     // 團隊管理員
    "team:" + teamId + ":specific:action" // 特定權限
));

// 避免：過於細粒度的權限檢查
@RequiresPermissions({"system:user:view:profile:basic:info"})  // 過度複雜
```

### 3. 權限維護策略

- **定期審查**: 清理未使用的權限定義
- **權限測試**: 為每個權限級別編寫測試用例
- **文檔同步**: 保持權限文檔與實現同步更新

---

## 總結

本系統的權限控管採用三層架構設計，支援靈活的權限分配和檢查。關鍵特點：

1. **萬用字元權限主導** - 約 80% 的權限檢查使用萬用字元
2. **雙重身份系統** - 系統身份 + 團隊身份並行運作
3. **OR 邏輯權限檢查** - 支援多級權限同時檢查
4. **動態權限生成** - 運行時根據用戶資料生成權限清單

通過理解這個架構，開發者可以：
- 正確實現新的權限檢查
- 診斷權限相關問題
- 優化權限分配策略
- 維護權限系統的一致性

---

*本文檔基於 2025-07-31 的程式碼分析結果，如有更新請同步修改。*