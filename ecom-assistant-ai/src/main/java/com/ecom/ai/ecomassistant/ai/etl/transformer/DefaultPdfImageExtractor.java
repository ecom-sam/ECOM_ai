package com.ecom.ai.ecomassistant.ai.etl.transformer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DefaultPdfImageExtractor implements EcomDocumentTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
        String filePath = documents.getFirst()
                .getMetadata()
                .getOrDefault(EcomDocumentTransformer.METADATA_FILE_FULL_PATH, "")
                .toString();

        if (StringUtils.isEmpty(filePath)) {
            return documents;
        }

        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            int pageNumber = 1;
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                int imageIndex = 1;
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject object = resources.getXObject(name);

                    if (! (object instanceof PDImageXObject image) ) {
                        continue;
                    }

                    String imageName = String.format("page_%s_number_%s", page, imageIndex);
                    Base64Result base64Result = imageToBase64(image, imageName);
                    if (base64Result == null) {
                        continue;
                    }
                    putResultToDocument(base64Result, documents.get(pageNumber - 1));
                    imageIndex++;
                }
                pageNumber++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return documents;
    }

    private Base64Result imageToBase64(PDImageXObject image, String name) throws IOException {
        BufferedImage bufferedImage = image.getImage();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            boolean isSuccess = ImageIO.write(bufferedImage, "png", outputStream);

            if (!isSuccess) {
                log.error("convert image failed");
                return null;
            }

            String base64 =  Base64.getEncoder().encodeToString(outputStream.toByteArray());
            if (StringUtils.isEmpty(base64)) {
                log.warn("base64 string is empty, image: {}", name);
                return null;
            }

            return new Base64Result(
                    name,
                    image.getCOSObject().getLength(),
                    base64
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void putResultToDocument(Base64Result base64Result, Document document) {
        Map<String, Object> metadata = document.getMetadata();
        ArrayList<Base64Result> imageReferenceList = (ArrayList<Base64Result>) metadata
                .getOrDefault(METADATA_IMAGE_REFERENCE, new ArrayList<>());
        imageReferenceList.add(base64Result);
        metadata.put(METADATA_IMAGE_REFERENCE, imageReferenceList);
    }
}