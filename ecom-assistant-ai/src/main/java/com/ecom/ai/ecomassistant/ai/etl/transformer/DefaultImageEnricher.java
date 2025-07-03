package com.ecom.ai.ecomassistant.ai.etl.transformer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultImageEnricher implements EcomDocumentTransformer {

    private final ChatClient helperChatClient;

    private final long minImageSize = 10 * 1024L;

    private final ExecutorService imageProcessingExecutor = Executors.newFixedThreadPool(32);

    private final String imageDescriptionTemplate = """
           **Please help identify the content of the image from a maintenance manual. The image may include a flowchart, plain text, mechanical structure diagram, or a screenshot.**
           
           - If the image is a **flowchart**, **list all steps using the original language** found in the image. **Do not add any descriptions.**
           - If the image contains **plain text**, **preserve the exact original text**, without adding any interpretation or description.
           - If the image is a **mechanical structure diagram**, provide a **brief description of each component** and **extract all labels or part names** shown.
           - If the image falls under **other categories**, provide a **short description of the visual elements** present.

           **Please output the result in Markdown format, and ensure all original text on the image is accurately preserved.**
           """;

    @Override
    public List<Document> transform(List<Document> documents) {
        return transformAsync(documents).join();
    }

    public CompletableFuture<List<Document>> transformAsync(List<Document> documents) {
        List<CompletableFuture<Document>> futures = new ArrayList<>();

        for (Document document : documents) {
            var imageRefObj = document.getMetadata().get(METADATA_IMAGE_REFERENCE);
            if (!(imageRefObj instanceof ArrayList<?>)) {
                // 沒有圖片的文件直接返回
                futures.add(CompletableFuture.completedFuture(document));
                continue;
            }

            // 有圖片的文件需要異步處理
            CompletableFuture<Document> documentFuture = processDocumentAsync(document, imageRefObj);
            futures.add(documentFuture);
        }

        // 等待所有異步任務完成
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    private CompletableFuture<Document> processDocumentAsync(Document document, Object imageRefObj) {
        return CompletableFuture.supplyAsync(() -> {
            assert document.getText() != null;
            StringBuilder stringBuilder = new StringBuilder(document.getText());

            @SuppressWarnings("unchecked")
            ArrayList<Base64Result> imageRefList = (ArrayList<Base64Result>) imageRefObj;

            String pageNumber = document.getMetadata().getOrDefault("page_number", "").toString();
            log.info("Starting process image, page: {}, size: {}", pageNumber, imageRefList.size());

            // 為每個圖片創建異步任務
            List<CompletableFuture<String>> imageProcessingFutures = imageRefList.stream()
                    .filter(base64Result -> base64Result.size() >= minImageSize)
                    .map(this::processImageAsync)
                    .toList();

            // 等待所有圖片處理完成
            List<String> imageDescriptions = imageProcessingFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            // 將圖片描述添加到文本中
            int imageNum = 1;
            for (String imageDescription : imageDescriptions) {
                String fileName = String.format("page_%s_%s", pageNumber, imageNum);
                stringBuilder.append("\n\n");
                stringBuilder.append(String.format("\n\nimage: [%s] \n\ncontent: (%s)", fileName, imageDescription));
                imageNum++;
            }

            return document.mutate()
                    .text(stringBuilder.toString())
                    .build();
        });
    }

    private CompletableFuture<String> processImageAsync(Base64Result base64Result) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] imageBytes = Base64.getDecoder().decode(base64Result.base64());
            Media media = Media.builder()
                    .mimeType(MimeTypeUtils.IMAGE_PNG)
                    .data(imageBytes)
                    .build();
            UserMessage userMessage = UserMessage.builder()
                    .media(media)
                    .text(imageDescriptionTemplate)
                    .build();

            return this.helperChatClient
                    .prompt()
                    .messages(userMessage)
                    .call()
                    .content();
        }, imageProcessingExecutor);
    }
}
