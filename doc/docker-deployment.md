# Docker 部署指南

本文檔包含完整的 Docker 部署選項和進階配置。

## 🐳 Docker 部署選項

### 1. 基本 Docker 部署
```bash
# 建立映像檔
docker build -f docker/Dockerfile -t ecom-assistant .

# 啟動容器
docker run --env-file .env -p 8080:8080 --name ecom-assistant ecom-assistant
```

### 2. 多平台建置 (Apple Silicon)
```bash
# 建立 buildx builder
docker buildx create --name multiarch-builder --use

# 多平台建置並推送
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t willyliang/ecom-assistant:latest \
  -f docker/Dockerfile \
  --push .
```

## 🔧 Couchbase 完整設定

### 完整的 Couchbase 啟動命令
```bash
docker run -d \
  --name couchbase-ai \
  --hostname couchbase.local \
  --add-host couchbase.local:127.0.0.1 \
  -p 8091-8097:8091-8097 \
  -p 9123:9123 \
  -p 11210:11210 \
  -p 11280:11280 \
  -p 18091-18097:18091-18097 \
  couchbase:enterprise-7.6.5
```

### 端口說明
| 端口 | 用途 |
|------|------|
| 8091-8097 | Couchbase Web Console 和 REST API |
| 9123 | 查詢服務 |
| 11210 | 數據服務 |
| 11280 | SSL 數據服務 |
| 18091-18097 | SSL Web Console 和 REST API |

## 🌐 Docker Compose 配置

### docker-compose.yml 示例
```yaml
version: '3.8'

services:
  couchbase:
    image: couchbase:enterprise-7.6.5
    container_name: couchbase-ai
    hostname: couchbase.local
    ports:
      - "8091-8097:8091-8097"
      - "9123:9123"
      - "11210:11210"
      - "11280:11280"
      - "18091-18097:18091-18097"
    volumes:
      - couchbase_data:/opt/couchbase/var
    environment:
      - CLUSTER_NAME=couchbase-ai
    extra_hosts:
      - "couchbase.local:127.0.0.1"

  ecom-assistant:
    build:
      context: .
      dockerfile: docker/Dockerfile
    container_name: ecom-assistant
    ports:
      - "8080:8080"
    depends_on:
      - couchbase
    env_file:
      - .env
    environment:
      - COUCHBASE_CONNECTION_STRINGS=couchbase

volumes:
  couchbase_data:
```

### 使用 Docker Compose 啟動
```bash
# 啟動所有服務
docker-compose up -d

# 查看日誌
docker-compose logs -f

# 停止服務
docker-compose down
```

## 🛠️ 進階配置

### 1. 生產環境建議
```bash
# 使用特定版本標籤
docker build -t ecom-assistant:1.0.0 .

# 限制資源使用
docker run -d \
  --name ecom-assistant \
  --memory=2g \
  --cpus=1.0 \
  -p 8080:8080 \
  --env-file .env \
  ecom-assistant:1.0.0
```

### 2. 開發環境配置
```bash
# 掛載源碼目錄進行開發
docker run -d \
  --name ecom-assistant-dev \
  -p 8080:8080 \
  -v $(pwd):/app \
  --env-file .env \
  ecom-assistant:latest
```

### 3. 日誌管理
```bash
# 配置日誌驅動
docker run -d \
  --name ecom-assistant \
  --log-driver=json-file \
  --log-opt max-size=100m \
  --log-opt max-file=3 \
  -p 8080:8080 \
  --env-file .env \
  ecom-assistant:latest
```

## 🔍 故障排除

### 常見 Docker 問題

#### 1. 容器啟動失敗
```bash
# 查看容器日誌
docker logs ecom-assistant

# 檢查容器狀態
docker ps -a
```

#### 2. 端口衝突
```bash
# 查看端口使用情況
netstat -an | grep 8080

# 使用不同端口
docker run -p 8081:8080 ecom-assistant
```

#### 3. 資源不足
```bash
# 檢查 Docker 資源使用
docker stats

# 清理未使用的映像
docker image prune -a
```

## 📋 相關文檔
- [主要 README](../README.md)
- [資料庫設定](setup_database.md)
- [認證授權系統](auth.md)