package com.ecom.ai.ecomassistant.ai;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.ai.document.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {

        extractImagesFromPdf(Path.of("/Users/willy/Downloads/Service Manual of HW5.02_TM25S_TM30S_EN_20250321.pdf"));
    }

    private static List<ExtractedImage> extractImagesFromPdf(Path filePath) throws IOException {
        List<ExtractedImage> extractedImages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            int pageNum = 0;

            for (PDPage page : document.getPages()) {
                pageNum++;
                PDResources resources = page.getResources();

                if (resources != null) {
                    int imageNum = 0;

                    for (COSName name : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(name);

                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;

                            // 生成圖片文件名
                            String imageFileName = String.format("page_%d_image_%d.%s",
                                    pageNum, ++imageNum, image.getSuffix());

                            System.out.println(imageFileName);

                            // 保存圖片
                            Path imagePath = saveImage(image, imageFileName);
//
//                            extractedImages.add(new ExtractedImage(
//                                    imagePath, pageNum, imageNum, image.getWidth(), image.getHeight()));
                        }
                    }
                }
            }
        }

        return extractedImages;
    }

    private static Path saveImage(PDImageXObject image, String fileName) throws IOException {
        Path imageDir = Paths.get("./");
        Files.createDirectories(imageDir);

        Path imagePath = imageDir.resolve(fileName);
        BufferedImage bufferedImage = image.getImage();
        ImageIO.write(bufferedImage, image.getSuffix(), imagePath.toFile());

        return imagePath;
    }

    public static class ExtractedImage {
        private final Path path;
        private final int pageNumber;
        private final int imageNumber;
        private final int width;
        private final int height;

        public ExtractedImage(Path path, int pageNumber, int imageNumber, int width, int height) {
            this.path = path;
            this.pageNumber = pageNumber;
            this.imageNumber = imageNumber;
            this.width = width;
            this.height = height;
        }

        // getters
        public Path getPath() { return path; }
        public int getPageNumber() { return pageNumber; }
        public int getImageNumber() { return imageNumber; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    public static class ImageAnalysisResult {
        private final ExtractedImage image;
        private final String analysis;

        public ImageAnalysisResult(ExtractedImage image, String analysis) {
            this.image = image;
            this.analysis = analysis;
        }

        // getters
        public ExtractedImage getImage() { return image; }
        public String getAnalysis() { return analysis; }
    }
}
