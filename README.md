# Spring Boot AI assistant

## 📦 專案架構
| 類別                    | 技術                    |
|-----------------------|-----------------------|
| 語言                    | Java 17               |
| 框架                    | Spring Boot 3.x       |
| ORM                   | Spring Data           | 
| 資料庫                   | Couchbase             |
| Cache server          | Couchbase             |
| Vector store          | Couchbase             |
| 驗證                    | Spring Security + JWT |
| AI 模型推論               | Spring AI             |
| Chat AI provider      | Groq                  |
| Embedding AI provider | OpenAI                |
| 文件                    | OpenAPI               |
| 建置工具                  | Maven                 |

## 🚀 快速啟動
### 1. 建立 `.env` 檔案
在專案根目錄，複製`.env.example`為`.env`：
```
COUCHBASE_CONNECTION_STRINGS=localhost
COUCHBASE_USERNAME=admin
COUCHBASE_PASSWORD=couchbase
COUCHBASE_BUCKET_NAME=ECOM
COUCHBASE_SCOPE_NAME=AI
COUCHBASE_VECTOR_COLLECTION_NAME=document

GROK_API_KEY=
GROK_CHAT_MODEL=meta-llama/llama-4-maverick-17b-128e-instruct

OPENAI_API_KEY=
```


### 2. 開發環境
run with config

| edit conifg                                    | add .env                                                   |
|------------------------------------------------|------------------------------------------------------------|
| ![edit_run_config](doc/md/edit_run_config.png) | ![edi_run_config_detail](doc/md/edi_run_config_detail.png) |
