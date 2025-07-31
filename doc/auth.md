# 認證授權系統文件

## 系統架構概述

本專案採用基於 **Apache Shiro** 和 **JWT** 的分散式認證授權架構，結合團隊制和權限系統，提供細粒度的存取控制。

### 核心技術組件
- **Apache Shiro**: 認證、授權、會話管理框架
- **JWT (JSON Web Token)**: 無狀態認證令牌
- **Couchbase**: 用戶資料和權限資料存儲
- **Spring Security**: 密碼加密（BCrypt）

### 整體架構圖

```mermaid
graph TB
    A[前端應用] -->|HTTP請求 + JWT| B[JwtFilter]
    B --> C{Token驗證}
    C -->|無效| D[返回401錯誤]
    C -->|有效| E[CouchbaseRealm]
    E --> F[認證資訊驗證]
    F --> G[授權資訊載入]
    G --> H{權限檢查}
    H -->|拒絕| I[返回403錯誤]
    H -->|通過| J[Controller]
    J --> K[Service Layer]
    K --> L[Repository]
    L --> M[(Couchbase)]
    
    N[UserManager] -->|登入成功| O[生成JWT]
    O --> P[返回Token]
    
    subgraph "Shiro安全框架"
        B
        E
        F
        G
        H
    end
    
    subgraph "數據存儲"
        M
    end
    
    style A fill:#e1f5fe
    style M fill:#f3e5f5
    style B fill:#fff3e0
    style E fill:#fff3e0
```

## 認證流程

### 1. 用戶登入流程

```mermaid
sequenceDiagram
    participant Client as 前端應用
    participant Controller as UserController
    participant UserManager as UserManager
    participant UserService as UserService
    participant JwtUtil as JwtUtil
    participant DB as Couchbase
    
    Client->>Controller: POST /api/v1/users/login
    Controller->>UserManager: login(email, password)
    UserManager->>UserService: findByEmail(email)
    UserService->>DB: 查詢用戶資料
    DB-->>UserService: User對象
    UserService-->>UserManager: User對象
    UserManager->>JwtUtil: generateToken(user)
    JwtUtil-->>UserManager: JWT Token
    UserManager-->>Controller: JWT Token
    Controller-->>Client: LoginResponse + Token
    
    Note over JwtUtil: Token包含: userId, systemRoles, 過期時間
```

**關鍵組件:**
- `JwtUtil:25` - JWT 令牌生成
- `UserManager` - 登入業務邏輯

### 2. 請求認證流程

```mermaid
sequenceDiagram
    participant Client as 前端應用
    participant JwtFilter as JwtFilter
    participant JwtUtil as JwtUtil
    participant CouchbaseRealm as CouchbaseRealm
    participant UserService as UserService
    participant Controller as Controller
    
    Client->>JwtFilter: HTTP請求 + Authorization Header
    JwtFilter->>JwtFilter: isLoginAttempt()
    JwtFilter->>JwtUtil: getTokenFromHeader()
    JwtUtil-->>JwtFilter: JWT Token
    JwtFilter->>JwtFilter: executeLogin()
    JwtFilter->>CouchbaseRealm: doGetAuthenticationInfo()
    CouchbaseRealm->>JwtUtil: validateToken(jwt)
    JwtUtil-->>CouchbaseRealm: 驗證結果
    
    alt Token有效
        CouchbaseRealm->>CouchbaseRealm: doGetAuthorizationInfo()
        CouchbaseRealm->>UserService: findById(userId)
        UserService-->>CouchbaseRealm: User + Roles + Permissions
        CouchbaseRealm-->>JwtFilter: AuthenticationInfo
        JwtFilter->>Controller: 繼續處理請求
        Controller-->>Client: 正常響應
    else Token無效
        CouchbaseRealm-->>JwtFilter: AuthenticationException
        JwtFilter-->>Client: 401 Unauthorized
    end
```

**關鍵組件:**
- `JwtFilter:21` - 攔截請求並驗證 JWT
- `CouchbaseRealm:35` - Shiro Realm 實作，處理認證邏輯
- `ShiroConfig:31` - Shiro 配置和過濾鏈定義

### 3. 當前用戶解析

透過 `@CurrentUserId` 註解自動解析當前用戶ID：

```mermaid
graph LR
    A[Controller方法] --> B["@CurrentUserId 註解"]
    B --> C[CurrentUserIdArgumentResolver]
    C --> D[提取Authorization Header]
    D --> E[JwtUtil.getTokenFromHeader]
    E --> F[JwtUtil.getUserId]
    F --> G[返回userId字符串]
    
    style B fill:#ffeb3b
    style C fill:#4caf50
```

```java
@GetMapping("/me")
public UserDetailDto me(@CurrentUserId String userId) {
    return userManager.getUserDetail(userId);
}
```

**實作機制:**
- `CurrentUserIdArgumentResolver:14` - Spring MVC 參數解析器
- 從 JWT Token 中提取 userId: `JwtUtil.getUserId(token)`

## 授權系統

### 權限分級架構

```mermaid
graph TB
    subgraph "權限層級體系"
        A[系統級權限<br/>SystemPermission] 
        B[團隊級權限<br/>TeamPermission]
        C[數據集權限<br/>DatasetPermission]
        
        A -.->|系統管理員擁有| B
        B -.->|團隊成員繼承| C
    end
    
    subgraph "系統級權限範疇"
        A1[SYSTEM_SUPER_ADMIN<br/>system:*]
        A2[SYSTEM_TEAM_ADMIN<br/>system:team:*]
        A3[SYSTEM_USER_ADMIN<br/>system:user:*]
        A4[SYSTEM_DATASET_ADMIN<br/>system:dataset:*]
        A5[SYSTEM_USER_INVITE<br/>system:user:invite]
        
        A --> A1
        A --> A2
        A --> A3
        A --> A4
        A --> A5
    end
    
    subgraph "團隊級權限範疇"
        B1[TEAM_VIEW<br/>team:view]
        B2[TEAM_EDIT<br/>team:edit]
        B3[TEAM_MEMBERS_INVITE<br/>members:invite]
        B4[TEAM_ROLES_MANAGE<br/>roles:manage]
        
        B --> B1
        B --> B2
        B --> B3
        B --> B4
    end
    
    subgraph "數據集權限範疇"
        C1[DATASET_VIEW<br/>dataset:view]
        C2[DATASET_MANAGE<br/>dataset:manage]
        C3[DATASET_FILE_UPLOAD<br/>dataset:file:upload]
        C4[DATASET_FILE_DELETE<br/>dataset:file:delete]
        
        C --> C1
        C --> C2
        C --> C3
        C --> C4
    end
    
    style A fill:#ffcdd2
    style B fill:#f8bbd9
    style C fill:#e1bee7
    style A1 fill:#ffeb3b
    style A2 fill:#ffeb3b
```

#### 1. 系統級權限 (SystemPermission)
管理整個系統的核心功能，具有最高優先級：

- `SYSTEM_SUPER_ADMIN("system:*")` - 系統超級管理權限
- `SYSTEM_TEAM_ADMIN("system:team:*")` - 團隊管理全部權限  
- `SYSTEM_USER_ADMIN("system:user:*")` - 使用者全部權限
- `SYSTEM_DATASET_ADMIN("system:dataset:*")` - 知識庫管理員
- `SYSTEM_USER_INVITE("system:user:invite")` - 邀請使用者
- `SYSTEM_TEAM_MANAGE("system:team:manage")` - 管理團隊

#### 2. 團隊級權限 (TeamPermission)
團隊內部管理權限，作用範圍限定在特定團隊：

- `TEAM_VIEW("team:view")` - 檢視團隊資訊
- `TEAM_EDIT("team:edit")` - 編輯團隊
- `TEAM_MEMBERS_INVITE("members:invite")` - 邀請團隊成員
- `TEAM_ROLES_MANAGE("roles:manage")` - 管理團隊角色

**團隊權限格式:** `team:{teamId}:{permission_code}`

#### 3. 數據集權限 (DatasetPermission)
知識庫相關操作權限，提供細粒度的資料存取控制：

- `DATASET_VIEW("dataset:view")` - 查詢知識庫
- `DATASET_MANAGE("dataset:manage")` - 知識庫基本訊息管理
- `DATASET_FILE_UPLOAD("dataset:file:upload")` - 上傳檔案
- `DATASET_FILE_DELETE("dataset:file:delete")` - 刪除其他人檔案

### 權限檢查機制

#### 授權流程圖

```mermaid
flowchart TD
    A[請求到達Controller] --> B{"方法有@RequiresPermissions?"}
    B -->|是| C[Shiro AOP攔截]
    B -->|否| D["檢查@ToolPermission"]
    
    C --> E[檢查系統/團隊權限]
    E --> F{權限檢查通過?}
    F -->|是| G[執行業務邏輯]
    F -->|否| H[拋出UnauthorizedException]
    
    D --> I{有工具權限註解?}
    I -->|是| J[PermissionService.hasToolPermission]
    I -->|否| G
    
    J --> K{角色檢查}
    K --> L{權限檢查}
    L --> M{AND/OR邏輯}
    M -->|通過| G
    M -->|失敗| H
    
    G --> N[程式化權限檢查]
    N --> O[PermissionUtil.checkXXX]
    O --> P{權限驗證}
    P -->|通過| Q[繼續執行]
    P -->|失敗| H
    
    H --> R[返回403 Forbidden]
    Q --> S[返回正常結果]
    
    style C fill:#ffeb3b
    style J fill:#4caf50
    style O fill:#2196f3
    style H fill:#f44336
```

#### 1. 註解式權限檢查

```java
@RequiresPermissions({"system:user:invite"})
public UserDto inviteUser(@RequestBody UserInviteRequest request) {
    // 方法實作
}
```

#### 2. 程式化權限檢查

```java
// 檢查單一權限
PermissionUtil.checkAnyPermission("system:user:view");

// 檢查多個權限 (OR 邏輯)  
PermissionUtil.checkAnyPermission(Set.of("perm1", "perm2"));

// 檢查多個權限 (AND 邏輯)
PermissionUtil.checkAllPermission(Set.of("perm1", "perm2"));
```

#### 3. 工具權限檢查

對於 AI 工具系統，支援動態權限檢查：

```java
@ToolPermission(
    roles = {"admin"},
    permissions = {"tool:execute"}, 
    roleLogic = LogicType.OR,
    permissionLogic = LogicType.AND
)
```

**實作位置:** `PermissionService:14`

## 用戶角色體系

### 角色解析流程

```mermaid
sequenceDiagram
    participant Request as HTTP請求
    participant CouchbaseRealm as CouchbaseRealm
    participant UserManager as UserManager
    participant UserService as UserService
    participant TeamService as TeamService
    participant DB as Couchbase
    
    Request->>CouchbaseRealm: doGetAuthorizationInfo()
    CouchbaseRealm->>UserManager: getUserRoleContext(user)
    UserManager->>UserService: findById(userId)
    UserService->>DB: 查詢用戶基本資料
    DB-->>UserService: User + SystemRoles
    
    UserManager->>TeamService: 查詢團隊成員關係
    TeamService->>DB: 查詢TeamMembership
    DB-->>TeamService: TeamMembership + TeamRoles
    
    UserManager->>UserManager: 整合系統角色和團隊權限
    UserManager-->>CouchbaseRealm: UserRoleContext{roles, permissions}
    CouchbaseRealm-->>Request: AuthorizationInfo
    
    Note over UserManager: 權限整合規則:<br/>系統權限 ∪ 團隊權限
```

**實作細節:**
- `CouchbaseRealm:57` - 取得用戶角色和權限
- `UserManager.getUserRoleContext()` - 整合系統角色和團隊權限

### 權限繼承規則

```mermaid
graph TD
    A[用戶] --> B[系統級角色]
    A --> C[團隊成員身份]
    
    B --> D[系統級權限]
    C --> E[團隊級權限]
    C --> F[數據集權限]
    
    D --> G[最終權限集合]
    E --> G
    F --> G
    
    subgraph "權限計算規則"
        H["系統權限 ∪ 團隊權限 ∪ 數據集權限"]
    end
    
    G --> H
    
    style A fill:#e3f2fd
    style G fill:#4caf50
    style H fill:#ffeb3b
```

1. **系統級角色** → 系統級權限
2. **團隊角色** → 團隊級權限 + 數據集權限  
3. **最終權限** = 系統權限 ∪ 團隊權限 ∪ 數據集權限

## 配置與安全

### Shiro 配置 (ShiroConfig)

```java
// 公開路徑 (無需認證)
chain.addPathDefinition("/swagger-ui/**", "anon");
chain.addPathDefinition("/**/users/login", "anon");
chain.addPathDefinition("/**/users/activate", "anon");

// API 路徑 (需要 JWT 認證)
chain.addPathDefinition("/**", "jwt");
```

### JWT 配置

```java
private static final String SECRET = "your-256-bit-secret"; // 需要配置為環境變數
private static final long EXPIRATION = 3600_000; // 1小時
```

**安全注意事項:**
- JWT 密鑰應存儲在環境變數中
- Token 過期時間可根據安全需求調整
- 支援 CORS 跨域請求

## 關鍵檔案說明

### 認證相關
- `ShiroConfig.java` - Shiro 主配置
- `JwtFilter.java` - JWT 認證過濾器
- `CouchbaseRealm.java` - Shiro Realm 實作
- `JwtUtil.java` - JWT 工具類

### 授權相關
- `PermissionDefinition.java` - 權限定義結構
- `PermissionRegistry.java` - 權限註冊表
- `SystemPermission.java` - 系統級權限枚舉
- `TeamPermission.java` - 團隊級權限枚舉
- `DatasetPermission.java` - 數據集權限枚舉
- `PermissionUtil.java` - 權限檢查工具
- `PermissionService.java` - 工具權限服務

### 用戶管理
- `UserManager.java` - 用戶業務邏輯
- `CurrentUserIdArgumentResolver.java` - 當前用戶ID解析器
- `CurrentUserId.java` - 當前用戶ID註解

## 最佳實踐

### 1. 權限設計原則
- **最小權限原則**: 用戶只擁有完成任務所需的最小權限
- **權限分層**: 系統 → 團隊 → 數據集的層次化權限結構
- **職責分離**: 不同類型的操作使用不同的權限控制

### 2. 安全配置建議
- JWT 密鑰使用至少 256 位的強隨機字符串
- 定期輪換 JWT 密鑰
- 合理設置 Token 過期時間
- 在生產環境中關閉詳細的錯誤信息

### 3. 權限檢查建議
- 在控制器層使用 `@RequiresPermissions` 註解
- 在服務層使用 `PermissionUtil` 進行程式化檢查
- 對於複雜的權限邏輯，優先使用專用的權限服務

### 4. 監控和審計
- 記錄關鍵的認證和授權事件
- 監控異常的權限檢查失敗
- 定期審查用戶權限分配