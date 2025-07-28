package com.ecom.ai.ecomassistant.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class QAPair {

    @Id
    private String id;

    @Field
    private String question;

    @Field
    private String answer;

    @Field
    private String datasetId;

    @Field
    private String datasetName;

    @Field
    private String documentName;

    @Field
    private String fileName;

    @Field
    private String documentId;

    @Field
    private Integer questionIndex;

    @Field
    private String contentType = "qa_pair";

    @Field
    private LocalDateTime createdAt;

    @Field
    private LocalDateTime updatedAt;
}