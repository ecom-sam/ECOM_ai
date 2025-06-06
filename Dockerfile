# ===== 1. 建構階段 =====
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# 設定工作目錄
WORKDIR /app

# 複製所有專案檔案到容器
COPY . .

# 編譯並打包，指定只打包 API 模組
RUN mvn clean install -DskipTests

# ===== 2. 運行階段 =====
FROM eclipse-temurin:21-jdk-alpine

# 設定工作目錄
WORKDIR /app

# 從 builder 複製 jar 到運行階段容器中
COPY --from=builder /app/ecom-assistant-api/target/ecom-assistant-api-*.jar app.jar

# 曝露服務埠（如果是 Spring Boot 預設埠）
EXPOSE 8080

# 設定容器啟動命令
ENTRYPOINT ["java", "-jar", "app.jar"]
