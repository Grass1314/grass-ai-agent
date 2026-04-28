package com.grass.grasspdftoimagemcpserver.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PdfToImageService {

    /**
     * 将 PDF 每页渲染为 base64 PNG 图片
     * @param pdfPath PDF 文件绝对路径
     * @param dpi     渲染精度，OCR 场景推荐 200
     */
    public List<PageImage> convertToImages(String pdfPath, float dpi) throws IOException {
        List<PageImage> result = new ArrayList<>();

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IllegalArgumentException("PDF文件不存在: " + pdfPath);
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String base64 = encodeToBase64(image);
                result.add(new PageImage(i + 1, totalPages, base64, "image/png"));
            }
        }

        return result;
    }


    private String encodeToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public record PageImage(int pageNumber, int totalPages, String base64, String mimeType) {}
}

