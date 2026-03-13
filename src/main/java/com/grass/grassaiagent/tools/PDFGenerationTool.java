package com.grass.grassaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.grass.grassaiagent.constant.FileConstant;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.FileOutputStream;

/**
 * @description: PDF生成工具，将文本内容生成为PDF文件（支持中文）
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class PDFGenerationTool {

    private static final String PDF_DIR = FileConstant.FILE_SAVE_DIR + "/pdf";

    @Tool(description = "Generate a PDF file from text content. Supports Chinese characters. Each paragraph should be separated by a newline.")
    public String generatePDF(
            @ToolParam(description = "File name for the PDF, e.g. 'report.pdf'") String fileName,
            @ToolParam(description = "Title of the PDF document") String title,
            @ToolParam(description = "Text content to write into the PDF, use \\n for new paragraphs") String content) {
        try {
            FileUtil.mkdir(PDF_DIR);
            if (!fileName.toLowerCase().endsWith(".pdf")) {
                fileName += ".pdf";
            }
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String filePath = PDF_DIR + "/" + fileName;

            // 中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font bodyFont = new Font(bfChinese, 12, Font.NORMAL);

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 标题
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20f);
            document.add(titleParagraph);

            // 正文：按换行分段
            String[] paragraphs = content.split("\\n");
            for (String para : paragraphs) {
                para = para.trim();
                if (para.isEmpty()) continue;
                Paragraph p = new Paragraph(para, bodyFont);
                p.setSpacingAfter(8f);
                p.setLeading(20f);
                p.setFirstLineIndent(24f);
                document.add(p);
            }

            document.close();

            long fileSize = FileUtil.size(FileUtil.file(filePath));
            return "PDF generated successfully.\n" +
                    "  Path: " + filePath + "\n" +
                    "  Size: " + FileUtil.readableFileSize(fileSize);
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
