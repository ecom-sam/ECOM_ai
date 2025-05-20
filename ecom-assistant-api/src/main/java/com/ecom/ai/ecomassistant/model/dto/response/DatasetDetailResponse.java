package com.ecom.ai.ecomassistant.model.dto.response;

import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.db.model.Document;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DatasetDetailResponse {
    private Dataset dataset;
    private List<Document> documents;
}
