package com.ecom.ai.ecomassistant.ai.etl.reader.json;

import com.ecom.ai.ecomassistant.ai.etl.reader.EcomDocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultJsonDocumentReader implements EcomDocumentReader {

    @Override
    public List<Document> read(Resource resource) {
        JsonReader jsonReader = new JsonReader(resource);
        return jsonReader.get();
    }
}
