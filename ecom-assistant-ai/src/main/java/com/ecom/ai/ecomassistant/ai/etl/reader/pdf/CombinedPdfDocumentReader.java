package com.ecom.ai.ecomassistant.ai.etl.reader.pdf;

import com.ecom.ai.ecomassistant.ai.config.FileProcessingRuleConfig;
import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import org.apache.pdfbox.cos.COSName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CombinedPdfDocumentReader implements EcomDocumentReader {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CombinedPdfDocumentReader.class);

    private final ChatModel chatModel;
    private final FileProcessingRuleConfig config;

    @Override
    public List<Document> read(Resource resource) {
        List<Document> documents = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(resource.getInputStream().readAllBytes())) {
            int totalPages = document.getNumberOfPages();
            log.info("Processing PDF with {} pages", totalPages);
            
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                
                // Combine text and image content while maintaining order
                String combinedContent;
                if (config.isEnablePositionSorting()) {
                    // 位置排序模式：提取文字和圖片位置，按座標排序
                    combinedContent = combineContentWithPosition(document, page, pageIndex);
                } else {
                    // 傳統模式：文字優先，然後圖片
                    String textContent = extractTextFromPage(document, pageIndex);
                    List<String> imageDescriptions = extractAndProcessImages(page, pageIndex);
                    combinedContent = combineContentTraditional(textContent, imageDescriptions, pageIndex);
                }
                
                if (!combinedContent.trim().isEmpty()) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("page", pageIndex + 1);
                    metadata.put("source", resource.getFilename());
                    metadata.put("content_type", "combined_text_image");
                    metadata.put("total_pages", totalPages);
                    
                    documents.add(new Document(combinedContent, metadata));
                }
            }
            
        } catch (IOException e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PDF document", e);
        }
        
        log.info("Generated {} documents from PDF", documents.size());
        return documents;
    }

    private String extractTextFromPage(PDDocument document, int pageIndex) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(pageIndex + 1);
        textStripper.setEndPage(pageIndex + 1);
        textStripper.setSortByPosition(true);
        
        String text = textStripper.getText(document);
        return text != null ? text.trim() : "";
    }

    private List<String> extractAndProcessImages(PDPage page, int pageIndex) {
        List<String> imageDescriptions = new ArrayList<>();
        
        try {
            PDResources resources = page.getResources();
            if (resources != null) {
                Iterable<COSName> xObjectNames = resources.getXObjectNames();
                
                for (COSName name : xObjectNames) {
                    PDXObject xObject = resources.getXObject(name);
                    
                    if (xObject instanceof PDImageXObject) {
                        PDImageXObject imageXObject = (PDImageXObject) xObject;
                        BufferedImage image = imageXObject.getImage();
                        
                        // Convert image to base64 for AI processing
                        String base64Image = convertImageToBase64(image);
                        
                        // Generate AI description for the image
                        String description = generateImageDescription(base64Image, pageIndex);
                        
                        log.info("Generated description for image on page {}: {}", pageIndex + 1, description);
                        
                        if (description != null && !description.trim().isEmpty()) {
                            imageDescriptions.add(description);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Error extracting images from page {}: {}", pageIndex + 1, e.getMessage());
        }
        
        return imageDescriptions;
    }

    private String convertImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String generateImageDescription(String base64Image, int pageIndex) {
        try {
            String prompt = """
                Please analyze this image from a PDF document and provide a comprehensive description.
                Focus on:
                1. Any text content visible in the image
                2. Process flows, diagrams, or organizational charts
                3. Key visual elements and their relationships
                4. Technical content, steps, or procedures
                5. Important data, numbers, or specific details
                
                If this appears to be a Standard Operating Procedure (SOP) or process flow:
                - Describe each step in the process
                - Identify decision points or branches
                - Note any important requirements or conditions
                
                Provide a detailed, comprehensive description that captures all important information.
                """;

            // Use OpenAI Vision API with standalone ChatClient (no memory)
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            org.springframework.core.io.Resource imageResource = 
                new org.springframework.core.io.ByteArrayResource(imageBytes);
            
            // Create Media object for vision analysis
            org.springframework.ai.content.Media imageMedia = new org.springframework.ai.content.Media(
                MimeTypeUtils.IMAGE_PNG, 
                imageResource
            );
            
            // Create standalone ChatClient without memory to avoid serialization issues
            ChatClient standaloneClient = ChatClient.builder(chatModel).build();
            
            String response = standaloneClient.prompt()
                .user(userSpec -> userSpec
                    .text(prompt)
                    .media(imageMedia))
                .call()
                .content();
            
            log.debug("Generated AI description for image on page {}: {} characters", 
                     pageIndex + 1, response.length());
            
            return response.trim();
                    
        } catch (Exception e) {
            log.warn("Failed to generate AI description for image on page {}: {}", pageIndex + 1, e.getMessage());
            return "圖片內容 (使用 AI 視覺分析時發生錯誤: " + e.getMessage() + ")";
        }
    }

    // 內容元素的抽象基類
    private static abstract class ContentElement {
        protected final float y;
        protected final float x;
        
        public ContentElement(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public float getY() { return y; }
        public float getX() { return x; }
        public abstract String getContent();
        public abstract String getType();
    }
    
    // 文字內容元素
    private static class TextElement extends ContentElement {
        private final String text;
        
        public TextElement(float x, float y, String text) {
            super(x, y);
            this.text = text;
        }
        
        @Override
        public String getContent() { return text; }
        
        @Override
        public String getType() { return "text"; }
    }
    
    // 圖片內容元素
    private static class ImageElement extends ContentElement {
        private final String description;
        private final int imageIndex;
        
        public ImageElement(float x, float y, String description, int imageIndex) {
            super(x, y);
            this.description = description;
            this.imageIndex = imageIndex;
        }
        
        @Override
        public String getContent() { 
            return "Image " + imageIndex + ": " + description; 
        }
        
        @Override
        public String getType() { return "image"; }
    }

    // 傳統模式：文字優先，然後圖片
    private String combineContentTraditional(String textContent, List<String> imageDescriptions, int pageIndex) {
        StringBuilder combined = new StringBuilder();
        
        // Add page header
        combined.append("=== Page ").append(pageIndex + 1).append(" ===\n\n");
        
        // Add text content first (maintaining original order)
        if (!textContent.isEmpty()) {
            combined.append("Text Content:\n");
            combined.append(textContent);
            combined.append("\n\n");
        }
        
        // Add image descriptions
        if (!imageDescriptions.isEmpty()) {
            combined.append("Image Content:\n");
            for (int i = 0; i < imageDescriptions.size(); i++) {
                combined.append("Image ").append(i + 1).append(": ");
                combined.append(imageDescriptions.get(i));
                combined.append("\n");
            }
            combined.append("\n");
        }
        
        return combined.toString();
    }
    
    // 位置排序模式：按 Y 座標排序文字和圖片
    private String combineContentWithPosition(PDDocument document, PDPage page, int pageIndex) {
        try {
            StringBuilder combined = new StringBuilder();
            combined.append("=== Page ").append(pageIndex + 1).append(" (Position Sorted) ===\n\n");
            
            // 提取文字位置
            List<TextElement> textElements = extractTextWithPosition(document, page, pageIndex);
            
            // 提取圖片位置
            List<ImageElement> imageElements = extractImagesWithPosition(page, pageIndex);
            
            // 合併所有元素
            List<ContentElement> allElements = new ArrayList<>();
            allElements.addAll(textElements);
            allElements.addAll(imageElements);
            
            if (allElements.isEmpty()) {
                return combined.toString();
            }
            
            // 按 Y 座標排序（從上到下，Y 座標大的在上面）
            allElements.sort((a, b) -> Float.compare(b.getY(), a.getY()));
            
            // 按順序組合內容
            for (ContentElement element : allElements) {
                combined.append(element.getContent()).append("\n");
                
                // 在不同類型元素之間加空行
                if (!allElements.get(allElements.size() - 1).equals(element)) {
                    int currentIndex = allElements.indexOf(element);
                    if (currentIndex < allElements.size() - 1) {
                        ContentElement nextElement = allElements.get(currentIndex + 1);
                        if (!element.getType().equals(nextElement.getType())) {
                            combined.append("\n");
                        }
                    }
                }
            }
            
            log.info("Position sorted content for page {}: {} text elements, {} image elements", 
                    pageIndex + 1, textElements.size(), imageElements.size());
            
            return combined.toString();
            
        } catch (Exception e) {
            log.warn("Position sorting failed for page {}, falling back to traditional mode: {}", 
                    pageIndex + 1, e.getMessage());
            // 失敗時回退到傳統模式
            try {
                String textContent = extractTextFromPage(document, pageIndex);
                List<String> imageDescriptions = extractAndProcessImages(page, pageIndex);
                return combineContentTraditional(textContent, imageDescriptions, pageIndex);
            } catch (Exception fallbackError) {
                log.error("Fallback also failed for page {}: {}", pageIndex + 1, fallbackError.getMessage());
                return "=== Page " + (pageIndex + 1) + " (Error) ===\n\n[Content extraction failed]";
            }
        }
    }
    
    // 提取文字和位置信息
    private List<TextElement> extractTextWithPosition(PDDocument document, PDPage page, int pageIndex) {
        List<TextElement> textElements = new ArrayList<>();
        
        try {
            // 提取頁面文字並按段落分割
            String pageText = extractTextFromPage(document, pageIndex);
            if (!pageText.trim().isEmpty()) {
                org.apache.pdfbox.pdmodel.common.PDRectangle mediaBox = page.getMediaBox();
                
                // 按段落分割文字（以雙換行或單換行分割）
                String[] paragraphs = pageText.split("\\n\\s*\\n|\\n");
                float startY = mediaBox.getHeight() * 0.9f; // 從頁面上方 90% 開始
                float lineHeight = 20; // 假設每行高度 20 點
                
                int elementCount = 0;
                for (String paragraph : paragraphs) {
                    String cleanParagraph = paragraph.trim();
                    if (!cleanParagraph.isEmpty() && cleanParagraph.length() > 2) {
                        float y = startY - (elementCount * lineHeight);
                        textElements.add(new TextElement(0, y, cleanParagraph));
                        elementCount++;
                        
                        // 避免創建太多元素，最多 10 個
                        if (elementCount >= 10) break;
                    }
                }
                
                // 如果沒有段落，就把整個文字當作一個元素
                if (textElements.isEmpty()) {
                    textElements.add(new TextElement(0, startY, pageText));
                }
            }
            
        } catch (Exception e) {
            log.warn("Error extracting text with position from page {}: {}", pageIndex + 1, e.getMessage());
        }
        
        return textElements;
    }
    
    // 提取圖片和位置信息
    private List<ImageElement> extractImagesWithPosition(PDPage page, int pageIndex) {
        List<ImageElement> imageElements = new ArrayList<>();
        
        try {
            PDResources resources = page.getResources();
            if (resources != null) {
                Iterable<COSName> xObjectNames = resources.getXObjectNames();
                int imageIndex = 1;
                
                // 收集所有圖片信息
                List<ImageInfo> imageInfos = new ArrayList<>();
                
                for (COSName name : xObjectNames) {
                    PDXObject xObject = resources.getXObject(name);
                    
                    if (xObject instanceof PDImageXObject) {
                        PDImageXObject imageXObject = (PDImageXObject) xObject;
                        
                        // 估算圖片位置（簡化版本）
                        org.apache.pdfbox.pdmodel.common.PDRectangle mediaBox = page.getMediaBox();
                        float x = 0; // 簡化：假設圖片在左邊
                        float y = mediaBox.getHeight() / 3; // 簡化：假設圖片在頁面下方
                        
                        BufferedImage image = imageXObject.getImage();
                        String base64Image = convertImageToBase64(image);
                        
                        imageInfos.add(new ImageInfo(x, y, base64Image, imageIndex));
                        imageIndex++;
                    }
                }
                
                // 根據配置決定是否分析圖片
                if (config.isEnableImageAnalysis()) {
                    // 並行處理圖片分析
                    imageElements = processImagesInParallel(imageInfos, pageIndex);
                } else {
                    // 不分析圖片，只記錄圖片存在
                    for (ImageInfo info : imageInfos) {
                        String description = "[圖片 " + info.index + " - AI 分析已停用]";
                        imageElements.add(new ImageElement(info.x, info.y, description, info.index));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting images with position from page {}: {}", pageIndex + 1, e.getMessage());
        }
        
        return imageElements;
    }
    
    // 圖片信息類別
    private static class ImageInfo {
        final float x, y;
        final String base64Image;
        final int index;
        
        ImageInfo(float x, float y, String base64Image, int index) {
            this.x = x;
            this.y = y;
            this.base64Image = base64Image;
            this.index = index;
        }
    }
    
    // 並行處理圖片分析
    private List<ImageElement> processImagesInParallel(List<ImageInfo> imageInfos, int pageIndex) {
        List<ImageElement> imageElements = new ArrayList<>();
        
        if (imageInfos.isEmpty()) {
            return imageElements;
        }
        
        log.info("Processing {} images in parallel for page {}", imageInfos.size(), pageIndex + 1);
        
        // 使用 CompletableFuture 並行處理
        List<java.util.concurrent.CompletableFuture<ImageElement>> futures = imageInfos.stream()
            .map(info -> java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    String description = generateImageDescription(info.base64Image, pageIndex);
                    if (description != null && !description.trim().isEmpty()) {
                        return new ImageElement(info.x, info.y, description, info.index);
                    } else {
                        return new ImageElement(info.x, info.y, "[圖片分析失敗]", info.index);
                    }
                } catch (Exception e) {
                    log.warn("Image analysis failed for image {} on page {}: {}", 
                            info.index, pageIndex + 1, e.getMessage());
                    return new ImageElement(info.x, info.y, "[圖片分析錯誤: " + e.getMessage() + "]", info.index);
                }
            }))
            .collect(Collectors.toList());
        
        // 等待所有圖片分析完成（帶超時）
        try {
            java.util.concurrent.CompletableFuture<Void> allFutures = 
                java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]));
            
            allFutures.get(config.getImageAnalysisTimeout(), java.util.concurrent.TimeUnit.SECONDS);
            
            // 收集結果
            for (java.util.concurrent.CompletableFuture<ImageElement> future : futures) {
                imageElements.add(future.get());
            }
            
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("Image analysis timeout for page {}, using partial results", pageIndex + 1);
            
            // 超時時收集已完成的結果
            for (java.util.concurrent.CompletableFuture<ImageElement> future : futures) {
                if (future.isDone()) {
                    try {
                        imageElements.add(future.get());
                    } catch (Exception ex) {
                        log.warn("Failed to get completed image result: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in parallel image processing for page {}: {}", pageIndex + 1, e.getMessage());
        }
        
        return imageElements;
    }
}