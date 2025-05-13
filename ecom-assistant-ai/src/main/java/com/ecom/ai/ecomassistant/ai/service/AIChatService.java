package com.ecom.ai.ecomassistant.ai.service;

import com.ecom.ai.ecomassistant.ai.DateTimeTools;
import com.ecom.ai.ecomassistant.ai.model.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private final ChatClient chatClient;

    public String toolTest() {
        return chatClient
                .prompt("我昨天 2020-05-01 14:00 買的產品保固日期到什麼時候？")
                .tools(new DateTimeTools())
                .call()
                .content();
    }

    public Flux<String> tellAJoke(String topic) {
        String prompt = """
                你是一個幽默的人，主要語言為台灣繁體中文，
                給我講一個關於{topic}的笑話
                """;

        return chatClient.prompt()
                .user(u -> u.text(prompt).param("topic", topic))
                .advisors(new SimpleLoggerAdvisor())
                .stream()
                .content();
    }

    public List<Book> recommendBooks(String category, String year) {
        String prompt = """
        請建議我幾本 {year} 年時 {category} 類型的暢銷書，
        如果有在台灣上市，請顯示繁體中文譯名。
        """;

        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("category", category)
                        .param("year", year)
                )
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
