package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.model.Book;
import com.ecom.ai.ecomassistant.service.AIChatService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping(value = "/joke", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tellAJoke(@RequestParam String topic) {
        return aiService.tellAJoke(topic);
    }

    @GetMapping("/books")
    public List<Book> recommendBooks(@RequestParam String category, @RequestParam String year) {
        return aiService.recommendBooks(category, year);
    }
}
