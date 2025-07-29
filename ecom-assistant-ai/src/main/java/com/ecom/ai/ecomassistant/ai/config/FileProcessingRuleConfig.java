package com.ecom.ai.ecomassistant.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "etl.file-processing.default")
public class FileProcessingRuleConfig {

    private String reader = "combinedPdfDocumentReader";
    private List<String> transformers = List.of(
//            "defaultPdfImageExtractor",
//            "defaultImageEnricher",
//            "defaultImageContentRemover",
            "defaultTokenTextSplitter"
    );
    
    // 新增：控制是否按位置排序頁面元素
    private boolean enablePositionSorting = true;
    
    // 新增：位置容差值（像素），用於判斷是否在同一行
    private float positionTolerance = 5.0f;
    
    // 新增：控制是否啟用圖片 AI 分析（預設關閉以提升效能）
    private boolean enableImageAnalysis = false;
    
    // 新增：圖片分析超時時間（秒）
    private int imageAnalysisTimeout = 30;
    
    // Getter methods for the new fields
    public boolean isEnablePositionSorting() {
        return enablePositionSorting;
    }
    
    public boolean isEnableImageAnalysis() {
        return enableImageAnalysis;
    }
    
    public int getImageAnalysisTimeout() {
        return imageAnalysisTimeout;
    }
}
