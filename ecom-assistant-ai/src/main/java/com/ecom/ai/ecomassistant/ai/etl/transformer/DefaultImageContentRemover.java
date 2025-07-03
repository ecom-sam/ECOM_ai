package com.ecom.ai.ecomassistant.ai.etl.transformer;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultImageContentRemover implements EcomDocumentTransformer {
    @Override
    public List<Document> transform(List<Document> documents) {

        for (Document document : documents) {
            document.getMetadata().remove(METADATA_IMAGE_REFERENCE);

            //TODO move to other collection
//            var imageRefObj = document.getMetadata().get(METADATA_IMAGE_REFERENCE);
//            if (!(imageRefObj instanceof ArrayList<?>)) {
//                continue;
//            }
//
//            @SuppressWarnings("unchecked")
//            ArrayList<Base64Result> imageRefList = (ArrayList<Base64Result>) imageRefObj;
        }

        return documents;
    }
}
