# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

This is a Spring Boot 3.x AI assistant application built with a multi-module Maven architecture:

- **ecom-assistant-api**: REST API controllers and web configuration (entry point)
- **ecom-assistant-core**: Core business logic and services, depends on all other modules
- **ecom-assistant-db**: Database entities, repositories, and data services (Couchbase)
- **ecom-assistant-ai**: AI-related services including ETL, document processing, and Spring AI integration
- **ecom-assistant-common**: Shared resources, DTOs, and common utilities

### Key Technologies
- Java 21 with Spring Boot 3.4.5
- Couchbase for database, cache, and vector storage
- Spring AI with OpenAI/Groq providers
- Spring Security with JWT authentication
- Apache Shiro for authorization
- MapStruct for entity mapping
- Maven for build management

### Authentication & Authorization
- JWT-based authentication with Apache Shiro realm
- Role-based access control with team memberships
- Permission system with dataset-level permissions
- Current user context resolution via `@CurrentUserId`

## Development Commands

### Build and Run
```bash
# Build entire project
mvn clean install

# Run application (from root directory)
mvn spring-boot:run -pl ecom-assistant-api

# Build Docker image
docker build -f docker/Dockerfile -t ecom-assistant .

# Multi-platform build (Apple Silicon)
docker buildx build --platform linux/amd64,linux/arm64 -t willyliang/ecom-assistant:latest .
```

### Environment Setup
1. Copy `.env.example` to `.env` and configure:
   - Couchbase connection details
   - OpenAI API key for embeddings
   - Groq API key for chat (optional)

2. Start Couchbase:
```bash
docker run -d --name couchbase-ai --hostname couchbase.local \
  --add-host couchbase.local:127.0.0.1 \
  -p 8091-8097:8091-8097 -p 9123:9123 -p 11210:11210 \
  -p 11280:11280 -p 18091-18097:18091-18097 \
  couchbase:enterprise-7.6.5
```

3. Initialize database schema: Run scripts in `schema/` directory in order

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl ecom-assistant-core
```

## Code Organization

### Module Dependencies
Modules are designed to be non-interdependent. Database entities remain in the db module, with DTOs in common module mapped via MapStruct.

### Key Patterns
- **Tool System**: Dynamic tool registration via `@ChatToolMarker` annotation
- **Permission System**: Declarative permissions using `PermissionDefinition` and `PermissionRegistry`
- **Event-Driven**: File upload events trigger AI processing pipelines
- **ETL Pipeline**: Configurable document readers and transformers for AI processing

### Important Services
- `ChatService` (ecom-assistant-core): Core chat functionality with RAG and streaming
- `AiChatController` (ecom-assistant-api): Main chat API controller (replaces deprecated ChatController)
- `DynamicToolService` (ecom-assistant-core): Tool registration and execution
- `PermissionService` (ecom-assistant-core): Authorization checks
- `EtlService` (ecom-assistant-ai): Document processing pipeline
- `DatasetService` (ecom-assistant-db): Dataset management

### Configuration
- Main config: `ecom-assistant-api/src/main/resources/application.yaml`
- Uses environment variables for sensitive data (API keys, database credentials)
- File upload directory configurable via `FILE_UPLOAD_DIR`

## AI Integration

### Chat System
- Spring AI integration with configurable providers (OpenAI/Groq)
- Memory management via Couchbase chat history
- Tool calling support with custom tools in `customtools/` package

### Document Processing
- ETL pipeline supports PDF, CSV, JSON document types
- Vector embeddings stored in Couchbase vector collections
- Image extraction and content processing capabilities

## API Documentation
Swagger UI available at: http://localhost:8080/swagger-ui/index.html