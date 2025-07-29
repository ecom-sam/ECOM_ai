package com.ecom.ai.ecomassistant.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Set;

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

    // 驗證狀態：PENDING, APPROVED, REJECTED
    @Field
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    // 驗證者ID
    @Field
    private String verifiedBy;

    // 驗證時間
    @Field
    private LocalDateTime verifiedAt;

    // 驗證備註
    @Field
    private String verificationNote;

    // 是否已向量化
    @Field
    private Boolean vectorized = false;

    // 繼承的 dataset tags
    @Field
    private Set<String> tags;

    // 向量化ID（在qa-vector collection中的ID）
    @Field
    private String vectorId;

    public enum VerificationStatus {
        PENDING,    // 待驗證
        APPROVED,   // 已通過
        REJECTED    // 已拒絕
    }
}