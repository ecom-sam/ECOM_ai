#!/bin/bash

# Couchbase 連接資訊
COUCHBASE_HOST="localhost:8094"
COUCHBASE_USER="Administrator"
COUCHBASE_PASSWORD="your_password"

# 建立 qa-vector 索引
curl -X PUT \
  "http://${COUCHBASE_HOST}/api/index/qa-vector-index" \
  -H "Content-Type: application/json" \
  -u "${COUCHBASE_USER}:${COUCHBASE_PASSWORD}" \
  -d @create-qa-vector-index.json

echo "索引建立完成！"