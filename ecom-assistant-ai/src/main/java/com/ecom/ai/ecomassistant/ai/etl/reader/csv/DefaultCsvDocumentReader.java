package com.ecom.ai.ecomassistant.ai.etl.reader.csv;

import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.Resource;

import java.util.List;

public class DefaultCsvDocumentReader implements EcomDocumentReader {
    @Override
    public List<Document> read(Resource resource) {
        //TODO csv to json
        JsonReader jsonReader = new JsonReader(resource);
        return jsonReader.get();
    }
}
