#!/bin/bash

echo "=== 測試權限問題 ==="

# 1. 首先用 super_admin 登入取得 token
echo "1. 登入 super_admin..."
SUPER_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"email": "super_admin@example.com", "password": "super_admin"}' | jq -r '.token')

if [ "$SUPER_TOKEN" = "null" ] || [ -z "$SUPER_TOKEN" ]; then
  echo "❌ super_admin 登入失敗"
  exit 1
fi

echo "✅ super_admin 登入成功"

# 2. 重設 sam0219mm@ecom.com 的密碼（如果需要）
echo "2. 重設 sam0219mm@ecom.com 用戶密碼..."
# 這裡可以添加重設密碼的邏輯，但需要相應的 API

# 3. 查看 sam0219mm@ecom.com 的詳細權限
echo "3. 查看 sam0219mm@ecom.com 的詳細信息..."
curl -s -H "Authorization: Bearer $SUPER_TOKEN" \
  "http://localhost:8080/api/v1/users?filter=sam0219mm" | jq '.data[0]'

# 4. 查看團隊成員詳情
echo "4. 查看團隊 c79e8f7a-7d4d-47d7-982e-e87b69df5ab5 的成員..."
curl -s -H "Authorization: Bearer $SUPER_TOKEN" \
  "http://localhost:8080/api/v1/teams/c79e8f7a-7d4d-47d7-982e-e87b69df5ab5/members" | jq

# 5. 查看自定義角色 "TEST" 的權限
echo "5. 查看自定義角色 TEST 的權限..."
curl -s -H "Authorization: Bearer $SUPER_TOKEN" \
  "http://localhost:8080/api/v1/teams/c79e8f7a-7d4d-47d7-982e-e87b69df5ab5/roles" | jq '.team[] | select(.name == "TEST")'

echo "=== 測試完成 ==="