package com.ecom.ai.ecomassistant.controller;

import com.ecom.ai.ecomassistant.event.file.AiFileUploadEvent;
import com.ecom.ai.ecomassistant.resource.file.FileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/etl")
public class EtlController {

    private final ApplicationEventPublisher publisher;

    @GetMapping
    public void test() {
        var event = AiFileUploadEvent.builder()
                .userId("")
                .datasetId("dset-123")
                .documentId("doc-123")
                .fileInfo(FileInfo.builder()
                        .fullPath("/Users/willy/Desktop/test.json")
                        .build()
                )
                .build();
        publisher.publishEvent(event);
    }
}
