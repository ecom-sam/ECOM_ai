// 前端除錯腳本 - 在瀏覽器控制台執行

console.log('=== Team Permissions Debug ===');

// 1. 檢查 localStorage 資料
const token = localStorage.getItem('token');
const userStr = localStorage.getItem('user');

console.log('1. LocalStorage Data:');
console.log('Token exists:', !!token);
console.log('User data:', userStr);

if (userStr) {
  try {
    const user = JSON.parse(userStr);
    console.log('User object:', user);
    console.log('User permissions:', user.permissions);
    console.log('User roles:', user.roles);
  } catch (e) {
    console.error('Failed to parse user data:', e);
  }
}

// 2. 測試權限檢查函數
if (userStr) {
  const user = JSON.parse(userStr);
  
  // 模擬 hasPermission 邏輯
  function testHasPermission(permission) {
    if (!user?.permissions) return false;
    
    const permissionsToCheck = Array.isArray(permission) ? permission : [permission];
    
    return permissionsToCheck.some(perm =>
      user.permissions.some((p) => {
        if (p === perm) return true;
        if (p.endsWith('*')) {
          const prefix = p.replace('*', '');
          return perm.startsWith(prefix);
        }
        return false;
      })
    );
  }
  
  console.log('2. Permission Tests:');
  console.log('Has system:team:*:', testHasPermission('system:team:*'));
  console.log('Has system:team:view:', testHasPermission('system:team:view'));
  console.log('Has system:team:manage:', testHasPermission('system:team:manage'));
  console.log('Has system:*:', testHasPermission('system:*'));
  
  // 測試團隊權限檢查的完整邏輯
  const teamPermissionCheck = testHasPermission([
    'system:*',
    'system:team:*',
    'system:team:view'
  ]);
  console.log('Team permission check result:', teamPermissionCheck);
}

// 3. 測試 API 請求
console.log('3. Testing API Requests:');

if (token) {
  // 測試用戶資訊 API
  fetch('/api/v1/users/me', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  .then(response => {
    console.log('Users/me API status:', response.status);
    return response.json();
  })
  .then(data => {
    console.log('Current user from API:', data);
    console.log('API permissions:', data.permissions);
  })
  .catch(err => console.error('Users/me API error:', err));
  
  // 測試團隊 API
  fetch('/api/v1/teams', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  .then(response => {
    console.log('Teams API status:', response.status);
    if (response.status === 403) {
      console.error('❌ Teams API returned 403 - Permission Denied');
      return response.text();
    }
    return response.json();
  })
  .then(data => {
    console.log('Teams API response:', data);
  })
  .catch(err => console.error('Teams API error:', err));
} else {
  console.log('❌ No token found - user not logged in');
}

console.log('=== Debug Complete ===');
console.log('💡 If Teams API returns 403, the user needs to re-login to get updated permissions');