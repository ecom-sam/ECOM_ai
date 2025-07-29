package com.ecom.ai.ecomassistant.ai.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ecom.ai.ecomassistant.ai.config.TieredRAGConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
public class TieredRAGService {

    private static final Logger log = LoggerFactory.getLogger(TieredRAGService.class);
    
    private final VectorStore qaVectorStore; // QA 專用向量存儲（qa-vector collection）
    private final VectorStore documentVectorStore; // 文檔向量存儲（document-vector collection）
    private final EmbeddingModel embeddingModel; // 用於生成查詢向量
    private final RestTemplate restTemplate; // 用於直接調用 FTS API
    private final ObjectMapper objectMapper; // JSON 處理
    private final TieredRAGConfig ragConfig; // 分層 RAG 配置

    public TieredRAGService(
            @Qualifier("qaVectorStore") VectorStore qaVectorStore,
            @Qualifier("vectorStore") VectorStore documentVectorStore,
            EmbeddingModel embeddingModel,
            TieredRAGConfig ragConfig) {
        this.qaVectorStore = qaVectorStore;
        this.documentVectorStore = documentVectorStore;
        this.embeddingModel = embeddingModel;
        this.ragConfig = ragConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // 啟動時記錄配置
        log.info("🔧 Tiered RAG Configuration:");
        log.info("  - Enabled: {}", ragConfig.isEnabled());
        log.info("  - Priority: {}", ragConfig.getPriority());
        log.info("  - QA Vector: enabled={}, topK={}, strategy={}", 
                ragConfig.getQaVector().isEnabled(), 
                ragConfig.getQaVector().getTopK(), 
                ragConfig.getQaVector().getSearchStrategy());
        log.info("  - Document Vector: enabled={}, topK={}, threshold={}", 
                ragConfig.getDocumentVector().isEnabled(), 
                ragConfig.getDocumentVector().getTopK(), 
                ragConfig.getDocumentVector().getSimilarityThreshold());
        log.info("  - Max Results: {}, Fallback: {}", 
                ragConfig.getSearch().getMaxTotalResults(),
                ragConfig.getSearch().isEnableFallback());
    }

    // 移除硬編碼的配置，改用 ragConfig 中的配置

    public List<Document> retrieve(String query) {
        // 無數據集過濾的預設實現
        return retrieveWithDatasetFilter(query, null);
    }

    /**
     * 分層檢索：根據配置決定 QA-vector 和 document-vector 的優先順序
     * 支援四種模式：qa-first, document-first, qa-only, document-only
     */
    public List<Document> retrieveWithDatasetFilter(String query, List<String> datasetIds) {
        // 檢查分層檢索是否啟用
        if (!ragConfig.isEnabled()) {
            log.info("Tiered RAG is disabled, using default document search only");
            return searchDocumentContent(query, datasetIds, ragConfig.getSearch().getMaxTotalResults());
        }

        List<Document> allResults = new ArrayList<>();
        
        try {
            log.info("🎯 Starting tiered retrieval with priority: {}", ragConfig.getPriority());
            log.info("🔧 QA Vector enabled: {}, Document Vector enabled: {}", 
                    ragConfig.getQaVector().isEnabled(), ragConfig.getDocumentVector().isEnabled());
            
            switch (ragConfig.getPriority()) {
                case QA_FIRST:
                    allResults = retrieveQAFirst(query, datasetIds);
                    break;
                case DOCUMENT_FIRST:
                    allResults = retrieveDocumentFirst(query, datasetIds);
                    break;
                case QA_ONLY:
                    log.info("🔍 QA_ONLY mode: QA Vector enabled = {}", ragConfig.getQaVector().isEnabled());
                    if (ragConfig.getQaVector().isEnabled()) {
                        allResults = searchQAContent(query, datasetIds);
                        log.info("🔍 QA_ONLY result: {} documents returned", allResults.size());
                    } else {
                        log.warn("🔍 QA_ONLY mode but QA Vector is disabled!");
                    }
                    break;
                case DOCUMENT_ONLY:
                    if (ragConfig.getDocumentVector().isEnabled()) {
                        allResults = searchDocumentContent(query, datasetIds, ragConfig.getSearch().getMaxTotalResults());
                    }
                    break;
            }

            // 記錄檢索統計
            int qaCount = (int) allResults.stream().filter(doc -> 
                    "qa_content".equals(doc.getMetadata().get("searchTier"))).count();
            int docCount = allResults.size() - qaCount;
            logRetrievalStats(query, qaCount, docCount, allResults.size());

            return allResults;

        } catch (Exception e) {
            log.error("Error in tiered RAG retrieval: {}", e.getMessage(), e);
            // 降級處理
            return handleRetrievalFallback(query, datasetIds);
        }
    }

    /**
     * QA 優先檢索模式
     */
    private List<Document> retrieveQAFirst(String query, List<String> datasetIds) {
        List<Document> allResults = new ArrayList<>();
        
        // 第一層：搜尋 QA 內容
        if (ragConfig.getQaVector().isEnabled()) {
            List<Document> qaResults = searchQAContent(query, datasetIds);
            if (!qaResults.isEmpty()) {
                log.info("Found {} QA results for query", qaResults.size());
                allResults.addAll(qaResults);
            }
        }

        // 第二層：搜尋文檔內容（補充）
        if (ragConfig.getDocumentVector().isEnabled()) {
            int remainingSlots = ragConfig.getSearch().getMaxTotalResults() - allResults.size();
            if (remainingSlots > 0) {
                List<Document> docResults = searchDocumentContent(query, datasetIds, remainingSlots);
                if (!docResults.isEmpty()) {
                    log.info("Found {} document results for query", docResults.size());
                    allResults.addAll(docResults);
                }
            }
        }
        
        return allResults;
    }

    /**
     * Document 優先檢索模式
     */
    private List<Document> retrieveDocumentFirst(String query, List<String> datasetIds) {
        List<Document> allResults = new ArrayList<>();
        
        // 第一層：搜尋文檔內容
        if (ragConfig.getDocumentVector().isEnabled()) {
            int docSlots = ragConfig.getSearch().getMaxTotalResults() - ragConfig.getQaVector().getTopK();
            List<Document> docResults = searchDocumentContent(query, datasetIds, Math.max(docSlots, 3));
            if (!docResults.isEmpty()) {
                log.info("Found {} document results for query", docResults.size());
                allResults.addAll(docResults);
            }
        }

        // 第二層：搜尋 QA 內容（補充）
        if (ragConfig.getQaVector().isEnabled()) {
            int remainingSlots = ragConfig.getSearch().getMaxTotalResults() - allResults.size();
            if (remainingSlots > 0) {
                List<Document> qaResults = searchQAContent(query, datasetIds);
                if (!qaResults.isEmpty()) {
                    log.info("Found {} QA results for query", qaResults.size());
                    // 只取剩餘的空間
                    allResults.addAll(qaResults.stream().limit(remainingSlots).toList());
                }
            }
        }
        
        return allResults;
    }

    /**
     * 檢索降級處理
     */
    private List<Document> handleRetrievalFallback(String query, List<String> datasetIds) {
        if (ragConfig.getSearch().isEnableFallback()) {
            log.warn("Using fallback: document-only search");
            return searchDocumentContent(query, datasetIds, ragConfig.getSearch().getMaxTotalResults());
        } else {
            log.error("Retrieval failed and fallback is disabled");
            return new ArrayList<>();
        }
    }

    /**
     * 搜尋 QA 內容（從 qa-vector collection）
     * 根據配置決定搜尋策略和順序
     */
    private List<Document> searchQAContent(String query, List<String> datasetIds) {
        // 檢查 QA 搜尋是否啟用
        if (!ragConfig.getQaVector().isEnabled()) {
            log.info("QA vector search is disabled, skipping QA search");
            return new ArrayList<>();
        }

        try {
            log.info("🔍 Starting QA search with strategy: {}", ragConfig.getQaVector().getSearchStrategy());
            
            List<Document> results = new ArrayList<>();
            
            switch (ragConfig.getQaVector().getSearchStrategy()) {
                case SPRING_AI_FIRST:
                    results = searchWithFallback(query, datasetIds, true);
                    break;
                case DIRECT_FTS_FIRST:
                    results = searchWithFallback(query, datasetIds, false);
                    break;
                case SPRING_AI_ONLY:
                    if (ragConfig.getQaVector().getSpringAi().isEnabled()) {
                        results = searchQAContentSpringAI(query, datasetIds);
                    }
                    break;
                case DIRECT_FTS_ONLY:
                    if (ragConfig.getQaVector().getDirectFts().isEnabled()) {
                        results = searchQAContentDirectFTS(query, datasetIds);
                    }
                    break;
            }
            
            // 為結果添加優先級標記和搜尋方法標記
            final List<Document> finalResults = results; // 創建 final 參考
            results.forEach(doc -> {
                doc.getMetadata().put("searchTier", "qa_content");
                doc.getMetadata().put("priority", "high");
                if (!finalResults.isEmpty()) {
                    // 檢查結果來源
                    if (doc.getMetadata().containsKey("source") && "qa-vector".equals(doc.getMetadata().get("source"))) {
                        doc.getMetadata().put("searchMethod", "DIRECT_FTS_API");
                    } else {
                        doc.getMetadata().put("searchMethod", "SPRING_AI_VECTORSTORE");
                    }
                }
            });

            // 最終日誌 - 清楚標示使用的搜尋方法
            if (!results.isEmpty()) {
                String method = results.get(0).getMetadata().containsKey("source") ? "DIRECT_FTS_API" : "SPRING_AI_VECTORSTORE";
                log.info("🎯 QA SEARCH SUCCESS: Found {} results using method: {}", results.size(), method);
            } else {
                log.warn("❌ QA SEARCH FAILED: Both Spring AI and Direct FTS returned 0 results");
            }

            return results;

        } catch (Exception e) {
            log.warn("Failed to search QA content: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 帶有自動降級的搜尋方法
     */
    private List<Document> searchWithFallback(String query, List<String> datasetIds, boolean springAiFirst) {
        List<Document> results = new ArrayList<>();
        
        if (springAiFirst) {
            // 先嘗試 Spring AI
            if (ragConfig.getQaVector().getSpringAi().isEnabled()) {
                results = searchQAContentSpringAI(query, datasetIds);
                if (results.isEmpty() && ragConfig.getSearch().isEnableFallback() && ragConfig.getQaVector().getDirectFts().isEnabled()) {
                    log.warn("Spring AI search failed, trying direct FTS API...");
                    results = searchQAContentDirectFTS(query, datasetIds);
                }
            }
        } else {
            // 先嘗試 Direct FTS
            if (ragConfig.getQaVector().getDirectFts().isEnabled()) {
                results = searchQAContentDirectFTS(query, datasetIds);
                if (results.isEmpty() && ragConfig.getSearch().isEnableFallback() && ragConfig.getQaVector().getSpringAi().isEnabled()) {
                    log.warn("Direct FTS search failed, trying Spring AI...");
                    results = searchQAContentSpringAI(query, datasetIds);
                }
            }
        }
        
        return results;
    }

    /**
     * 使用 Spring AI VectorStore 搜尋 QA 內容
     */
    private List<Document> searchQAContentSpringAI(String query, List<String> datasetIds) {
        try {
            log.info("📱 Using Spring AI VectorStore for QA search");
            
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(query)
                    .topK(ragConfig.getQaVector().getTopK())
                    .similarityThreshold(ragConfig.getQaVector().getSpringAi().getSimilarityThreshold());

            SearchRequest searchRequest = searchBuilder.build();
            log.info("Spring AI request - TopK: {}, Threshold: {}", 
                    ragConfig.getQaVector().getTopK(), ragConfig.getQaVector().getSpringAi().getSimilarityThreshold());
            
            List<Document> results = qaVectorStore.similaritySearch(searchRequest);
            log.info("Spring AI QA search returned {} results", results.size());
            
            return results;

        } catch (Exception e) {
            log.error("Spring AI QA search failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 直接使用 Couchbase FTS API 進行 QA 向量搜尋
     */
    private List<Document> searchQAContentDirectFTS(String query, List<String> datasetIds) {
        try {
            log.info("🔗 Using Direct FTS API for QA search");
            
            // 生成查詢向量
            float[] queryVectorFloat = embeddingModel.embed(query);
            log.info("Generated query vector with {} dimensions", queryVectorFloat.length);

            // 轉換為 Double[] 以確保 JSON 序列化正確
            Double[] queryVector = new Double[queryVectorFloat.length];
            for (int i = 0; i < queryVectorFloat.length; i++) {
                queryVector[i] = (double) queryVectorFloat[i];
            }

            // 構建 FTS 查詢
            Map<String, Object> ftsQuery = new HashMap<>();
            ftsQuery.put("size", ragConfig.getQaVector().getTopK());
            
            // KNN 搜尋
            List<Map<String, Object>> knnQueries = new ArrayList<>();
            Map<String, Object> knnQuery = new HashMap<>();
            knnQuery.put("field", "embedding");
            knnQuery.put("vector", queryVector);
            knnQuery.put("k", ragConfig.getQaVector().getTopK());
            knnQueries.add(knnQuery);
            ftsQuery.put("knn", knnQueries);

            // 發送請求到 FTS
            String ftsUrl = "http://localhost:8094/api/index/" + ragConfig.getQaVector().getDirectFts().getIndexName() + "/query";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Basic " + 
                Base64.getEncoder().encodeToString("Administrator:password".getBytes()));

            // 記錄請求詳情
            log.info("🔍 Direct FTS Request: URL={}, QueryVector.length={}", ftsUrl, queryVector.length);
            log.debug("FTS Query: {}", objectMapper.writeValueAsString(ftsQuery));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ftsQuery, headers);
            ResponseEntity<String> response = restTemplate.exchange(ftsUrl, HttpMethod.POST, entity, String.class);

            log.info("📡 FTS API response status: {}", response.getStatusCode());
            log.debug("FTS Response: {}", response.getBody());
            
            // 解析回應
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode hits = responseJson.get("hits");
            
            List<Document> documents = new ArrayList<>();
            if (hits != null && hits.isArray()) {
                log.info("FTS API returned {} hits", hits.size());
                for (JsonNode hit : hits) {
                    String docId = hit.get("id").asText();
                    double score = hit.get("score").asDouble();
                    
                    // 從 Couchbase 獲取完整文檔內容
                    Document doc = getQADocumentById(docId);
                    if (doc != null) {
                        doc.getMetadata().put("score", score);
                        documents.add(doc);
                    }
                }
            }

            log.info("Direct FTS search returned {} documents", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Direct FTS search failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根據 ID 從 qa-vector collection 獲取文檔
     */
    private Document getQADocumentById(String docId) {
        try {
            String n1qlUrl = "http://localhost:8093/query/service";
            String query = String.format(
                "SELECT content FROM `ECOM`.`AI`.`qa-vector` USE KEYS '%s'", docId);
            
            Map<String, Object> queryRequest = new HashMap<>();
            queryRequest.put("statement", query);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Basic " + 
                Base64.getEncoder().encodeToString("Administrator:password".getBytes()));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(queryRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(n1qlUrl, HttpMethod.POST, entity, String.class);

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode results = responseJson.get("results");
            
            if (results != null && results.isArray() && results.size() > 0) {
                String content = results.get(0).get("content").asText();
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("id", docId);
                metadata.put("source", "qa-vector");
                return new Document(content, metadata);
            }

        } catch (Exception e) {
            log.error("Failed to get QA document by ID {}: {}", docId, e.getMessage());
        }
        return null;
    }

    /**
     * 搜尋文檔內容（從 document-vector collection）
     */
    private List<Document> searchDocumentContent(String query, List<String> datasetIds, int maxResults) {
        try {
            SearchRequest.Builder searchBuilder = SearchRequest.builder()
                    .query(query)
                    .topK(maxResults)
                    .similarityThreshold(ragConfig.getDocumentVector().getSimilarityThreshold());

            // 只需要添加數據集過濾（整個 collection 都是文檔內容）
            if (datasetIds != null && !datasetIds.isEmpty()) {
                String datasetFilter = createDatasetFilterString(datasetIds);
                searchBuilder.filterExpression(datasetFilter);
            }

            List<Document> results = documentVectorStore.similaritySearch(searchBuilder.build());
            
            // 為結果添加優先級標記
            results.forEach(doc -> {
                doc.getMetadata().put("searchTier", "document_content");
                doc.getMetadata().put("priority", "normal");
            });

            return results;

        } catch (Exception e) {
            log.warn("Failed to search document content: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 建立數據集過濾器字符串
     */
    private String createDatasetFilterString(List<String> datasetIds) {
        String datasetIdString = datasetIds.stream()
                .map(s -> String.format("'%s'", s))
                .collect(Collectors.joining(", "));
        
        return String.format("datasetId IN [%s]", datasetIdString);
    }

    /**
     * 記錄檢索統計資訊
     */
    private void logRetrievalStats(String query, int qaCount, int docCount, int totalCount) {
        log.info("Tiered RAG retrieval completed - Query: '{}', QA: {}, Doc: {}, Total: {}", 
                query.length() > 30 ? query.substring(0, 30) + "..." : query,
                qaCount, docCount, totalCount);
    }

    /**
     * 獲取配置資訊（用於監控和調試）
     */
    public TieredRAGConfig getConfig() {
        return ragConfig;
    }
}