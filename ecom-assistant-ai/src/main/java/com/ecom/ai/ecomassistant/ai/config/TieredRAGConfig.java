package com.ecom.ai.ecomassistant.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag.tiered-search")
public class TieredRAGConfig {

    /**
     * 是否啟用分層檢索（總開關）
     */
    private boolean enabled = true;

    /**
     * 檢索優先順序：
     * - qa-first: 優先檢索 QA-vector，然後 document-vector
     * - document-first: 優先檢索 document-vector，然後 QA-vector  
     * - qa-only: 僅檢索 QA-vector
     * - document-only: 僅檢索 document-vector
     */
    private RetrievalPriority priority = RetrievalPriority.QA_FIRST;

    /**
     * QA 向量檢索配置
     */
    private QAVectorConfig qaVector = new QAVectorConfig();

    /**
     * Document 向量檢索配置
     */
    private DocumentVectorConfig documentVector = new DocumentVectorConfig();

    /**
     * 搜尋參數配置
     */
    private SearchConfig search = new SearchConfig();

    @Data
    public static class QAVectorConfig {
        private boolean enabled = true;
        private int topK = 3;
        private QASearchStrategy searchStrategy = QASearchStrategy.DIRECT_FTS_FIRST;
        private SpringAIConfig springAi = new SpringAIConfig();
        private DirectFTSConfig directFts = new DirectFTSConfig();
    }

    @Data
    public static class DocumentVectorConfig {
        private boolean enabled = true;
        private int topK = 6;
        private double similarityThreshold = 0.30;
    }

    @Data
    public static class SpringAIConfig {
        private boolean enabled = true;
        private double similarityThreshold = 0.3;
    }

    @Data
    public static class DirectFTSConfig {
        private boolean enabled = true;
        private String indexName = "ECOM-AI-qa-vector-index";
    }

    @Data
    public static class SearchConfig {
        private int maxTotalResults = 8;
        private boolean enableFallback = true;
    }

    public enum RetrievalPriority {
        QA_FIRST("qa-first"),
        DOCUMENT_FIRST("document-first"),
        QA_ONLY("qa-only"),
        DOCUMENT_ONLY("document-only");

        private final String value;

        RetrievalPriority(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum QASearchStrategy {
        SPRING_AI_FIRST("spring-ai-first"),
        DIRECT_FTS_FIRST("direct-fts-first"),
        SPRING_AI_ONLY("spring-ai-only"),
        DIRECT_FTS_ONLY("direct-fts-only");

        private final String value;

        QASearchStrategy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}