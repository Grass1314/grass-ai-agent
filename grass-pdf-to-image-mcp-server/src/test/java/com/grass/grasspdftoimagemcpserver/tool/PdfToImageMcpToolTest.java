package com.grass.grasspdftoimagemcpserver.tool;

import com.grass.grasspdftoimagemcpserver.constant.FileConstant;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PdfToImageMcpToolTest {

    @Resource
    private PdfToImageMcpTool pdfToImageMcpTool;

    private static final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/files/" + System.currentTimeMillis();

    @Test
    void pdfToImagesForOcr() {
        List<McpSchema.Content> contents = pdfToImageMcpTool.pdfToImagesForOcr("/Users/grass/Documents/Code/Study/project/grass-ai-agent/grass-pdf-to-image-mcp-server/src/main/resources/template/《员工外派学习审批表（总部）》.pdf", 200);
        assertNotNull(contents);
        System.out.println(contents);
    }

    @Test
    void pdfToImagesAndSave() {
        List<McpSchema.Content> contents = pdfToImageMcpTool.pdfToImagesAndSave("/Users/grass/Documents/Code/Study/project/grass-ai-agent/grass-pdf-to-image-mcp-server/src/main/resources/template/满意度评估汇总.pdf", FILE_DIR, 300);
        assertNotNull(contents);
        System.out.println(contents);
    }
}