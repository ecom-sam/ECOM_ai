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
//        var event = new AiFileUploadEvent();
//        event.setDatasetId("W-dset-123");
//        event.setDocumentId("w-doc-123");
//        event.setFileInfo(FileInfo.builder()
//                .fullPath("/Users/willy/Desktop/test.json")
//                .build()
//        );
        var event = new AiFileUploadEvent(
                "dummy-user-id",
                "W-dset-123",
                "w-doc-123"
        );
        event.setFileInfo(FileInfo.builder()
                .fullPath("/Users/willy/Desktop/test.json")
                .build()
        );

        publisher.publishEvent(event);
    }
}
