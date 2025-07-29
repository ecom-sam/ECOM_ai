package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import com.ecom.ai.ecomassistant.db.service.QAPairService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QAGenerationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QAGenerationService.class);

    private final ChatModel chatModel;
    private final QAPairService qaPairService;

    public static class QAPairInfo {
        private final String question;
        private final String answer;

        public QAPairInfo(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }

    public List<QAPair> generateAndSaveQAPairs(List<Document> documents, String datasetId, String datasetName, 
                                               String documentName, String fileName, String documentId, Set<String> datasetTags) {
        try {
            log.info("Generating Q/A pairs for entire document: {} (dataset: {}, {} pages)", fileName, datasetName, documents.size());

            // Generate Q/A pairs for the ENTIRE document (not per page)
            List<QAPairInfo> qaPairInfos = generateQAPairsForEntireDocument(documents, datasetName, fileName);
            
            if (qaPairInfos.isEmpty()) {
                log.warn("No Q/A pairs generated for document: {}", fileName);
                return new ArrayList<>();
            }

            // Convert to QAPair entities and save to database
            List<QAPair> qaPairs = new ArrayList<>();
            for (int i = 0; i < qaPairInfos.size(); i++) {
                QAPairInfo info = qaPairInfos.get(i);
                QAPair qaPair = QAPair.builder()
                        .question(info.getQuestion())
                        .answer(info.getAnswer())
                        .datasetId(datasetId)
                        .datasetName(datasetName)
                        .documentName(documentName)
                        .fileName(fileName)
                        .documentId(documentId)
                        .questionIndex(i + 1)
                        .contentType("qa_pair")
                        .tags(datasetTags)  // 繼承 dataset 的 tags
                        .verificationStatus(QAPair.VerificationStatus.PENDING)  // 設為待驗證狀態
                        .vectorized(false)  // 未向量化
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                qaPairs.add(qaPair);
            }

            // Replace existing Q/A pairs for this document
            qaPairService.replaceQAPairsForDocument(documentId, qaPairs);
            
            log.info("Successfully generated and saved {} Q/A pairs for entire document: {}", qaPairs.size(), fileName);
            return qaPairs;

        } catch (Exception e) {
            log.error("Error generating and saving Q/A pairs for document {}: {}", fileName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<QAPairInfo> generateQAPairsForEntireDocument(List<Document> documents, String datasetName, String fileName) {
        try {
            // Combine all document content
            StringBuilder fullContent = new StringBuilder();
            for (Document doc : documents) {
                fullContent.append(doc.getFormattedContent()).append("\n\n");
            }

            String content = fullContent.toString().trim();
            if (content.isEmpty()) {
                log.warn("No content found in documents for Q/A generation");
                return new ArrayList<>();
            }

            log.info("Document content for Q/A generation: {}", content);

            log.info("Generating 10 Q/A pairs for entire document: {} (total content length: {} characters, {} pages)", 
                    fileName, content.length(), documents.size());

            // Generate Q/A pairs using AI - create standalone ChatClient without memory
            String prompt = createQAPrompt(content, fileName);
            ChatClient standaloneClient = ChatClient.builder(chatModel).build();
            String response = standaloneClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI Response for Q/A generation (length: {}): {}", response.length(), response);

            // Parse the response into Q/A pairs
            return parseQAResponse(response);

        } catch (Exception e) {
            log.error("Error generating Q/A pairs for document {}: {}", fileName, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String createQAPrompt(String content, String fileName) {
        return String.format("""
            Please analyze the following document content and generate exactly 10 comprehensive question-answer pairs.
            
            Document: %s
            
            Content:
            %s
            
            Requirements:
            1. Generate exactly 10 questions that cover the key topics and important information in the document
            2. Questions should be diverse and cover different aspects of the content
            3. Answers should be comprehensive and detailed, drawing from the document content
            4. Focus on the most important and useful information
            5. Include questions about main concepts, key details, processes, and conclusions
            
            Format your response as follows:
            Q1: [Question 1]
            A1: [Detailed Answer 1]
            
            Q2: [Question 2]
            A2: [Detailed Answer 2]
            
            ... continue for all 10 Q/A pairs
            
            Please ensure each answer is substantial and informative, providing value to someone learning about this content.
            """, fileName, content);
    }

    private List<QAPairInfo> parseQAResponse(String response) {
        List<QAPairInfo> qaPairs = new ArrayList<>();
        
        if (response == null || response.trim().isEmpty()) {
            log.warn("Empty response from AI for Q/A generation");
            return qaPairs;
        }

        try {
            String[] lines = response.split("\n");
            String currentQuestion = null;
            StringBuilder currentAnswer = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.matches("^Q\\d+:.*")) {
                    // Save previous Q/A pair if exists
                    if (currentQuestion != null && currentAnswer.length() > 0) {
                        qaPairs.add(new QAPairInfo(currentQuestion, currentAnswer.toString().trim()));
                    }
                    
                    // Start new question
                    currentQuestion = line.substring(line.indexOf(":") + 1).trim();
                    currentAnswer = new StringBuilder();
                    
                } else if (line.matches("^A\\d+:.*")) {
                    // Start answer for current question
                    currentAnswer.append(line.substring(line.indexOf(":") + 1).trim());
                    
                } else if (!line.isEmpty() && currentAnswer.length() > 0) {
                    // Continue current answer
                    currentAnswer.append(" ").append(line);
                }
            }
            
            // Add the last Q/A pair
            if (currentQuestion != null && currentAnswer.length() > 0) {
                qaPairs.add(new QAPairInfo(currentQuestion, currentAnswer.toString().trim()));
            }
            
        } catch (Exception e) {
            log.error("Error parsing Q/A response: {}", e.getMessage(), e);
        }

        log.info("Successfully parsed {} Q/A pairs", qaPairs.size());
        return qaPairs;
    }
}