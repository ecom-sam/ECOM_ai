package com.ecom.ai.ecomassistant.db.repository;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import org.springframework.data.couchbase.repository.Collection;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Collection("qa")
public interface QAPairRepository extends CouchbaseRepository<QAPair, String> {

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.datasetId = $1 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDatasetIdOrderByQuestionIndex(String datasetId);

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.documentId = $1 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDocumentIdOrderByQuestionIndex(String documentId);

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.datasetId = $1 AND qa.documentName = $2 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDatasetIdAndDocumentNameOrderByQuestionIndex(String datasetId, String documentName);

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.datasetName = $1 ORDER BY qa.documentName ASC, qa.questionIndex ASC")
    List<QAPair> findByDatasetNameOrderByDocumentNameAndQuestionIndex(String datasetName);

    @Query("DELETE FROM `ECOM`.`AI`.`qa` WHERE documentId = $1")
    void deleteByDocumentId(String documentId);

    @Query("DELETE FROM `ECOM`.`AI`.`qa` WHERE datasetId = $1")
    void deleteByDatasetId(String datasetId);

    // 驗證相關查詢
    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.verificationStatus = $1 ORDER BY qa.createdAt DESC")
    Page<QAPair> findByVerificationStatus(QAPair.VerificationStatus status, Pageable pageable);

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.datasetId = $1 AND qa.verificationStatus = $2 ORDER BY qa.createdAt DESC")
    List<QAPair> findByDatasetIdAndVerificationStatus(String datasetId, QAPair.VerificationStatus status);

    @Query("SELECT `_class`, META(`qa`).`id` AS __id, `question`, `answer`, `datasetId`, `datasetName`, `documentName`, `fileName`, `documentId`, `questionIndex`, `contentType`, `createdAt`, `updatedAt`, `verificationStatus`, `verifiedBy`, `verifiedAt`, `verificationNote`, `vectorized`, `tags`, `vectorId` FROM `ECOM`.`AI`.`qa` qa WHERE qa.verificationStatus = 'APPROVED' AND qa.vectorized = false ORDER BY qa.createdAt ASC")
    List<QAPair> findApprovedNotVectorized();

    @Query("SELECT COUNT(*) FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} qa WHERE qa.verificationStatus = 'PENDING'")
    Long countPendingQAs();
}