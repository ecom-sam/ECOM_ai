package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.ai.model.Book;
import com.ecom.ai.ecomassistant.ai.service.AIChatService;
import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.core.ApplicationPushBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class AIChatController {

    private final AIChatService aiService;

    private final ApplicationEventPublisher publisher;

    @GetMapping(value = "/joke", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tellAJoke(@RequestParam String topic) {
        return aiService.tellAJoke(topic);
    }

    @GetMapping("/books")
    public List<Book> recommendBooks(@RequestParam String category, @RequestParam String year) {
        return aiService.recommendBooks(category, year);
    }

    @GetMapping(value = "/tools")
    public String toolTest() {
        return aiService.toolTest();
    }

    @GetMapping("/test")
    public void test() {
        var event = new AiFileUploadEvent();
        event.setFullPath("/Users/willy/Desktop/project/Couchbase/TechmanRobot/OneDrive_1_2025-3-28/TT_AXM_CFC_250305 明基健康生活_發票底稿出貨應收作業_v00.pdf");
        publisher.publishEvent(event);
    }
}
