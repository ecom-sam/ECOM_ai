package com.ecom.ai.ecomassistant.db.repository;

import com.ecom.ai.ecomassistant.db.model.QAPair;
import org.springframework.data.couchbase.repository.Collection;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Collection("qa")
public interface QAPairRepository extends CouchbaseRepository<QAPair, String> {

    @Query("SELECT qa.* FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} qa WHERE qa.datasetId = $1 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDatasetIdOrderByQuestionIndex(String datasetId);

    @Query("SELECT qa.* FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} qa WHERE qa.documentId = $1 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDocumentIdOrderByQuestionIndex(String documentId);

    @Query("SELECT qa.* FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} qa WHERE qa.datasetId = $1 AND qa.documentName = $2 ORDER BY qa.questionIndex ASC")
    List<QAPair> findByDatasetIdAndDocumentNameOrderByQuestionIndex(String datasetId, String documentName);

    @Query("SELECT qa.* FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} qa WHERE qa.datasetName = $1 ORDER BY qa.documentName ASC, qa.questionIndex ASC")
    List<QAPair> findByDatasetNameOrderByDocumentNameAndQuestionIndex(String datasetName);

    @Query("DELETE FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} WHERE documentId = $1")
    void deleteByDocumentId(String documentId);

    @Query("DELETE FROM #{#n1ql.bucket}.#{#n1ql.scope}.#{#n1ql.collection} WHERE datasetId = $1")
    void deleteByDatasetId(String datasetId);
}