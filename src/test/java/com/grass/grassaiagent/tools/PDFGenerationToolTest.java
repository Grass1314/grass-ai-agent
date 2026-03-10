package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PDFGenerationToolTest {

    private PDFGenerationTool tool;

    @BeforeEach
    public void setUp() {
        tool = new PDFGenerationTool();
    }

    @Test
    public void testGeneratePDFChinese() {
        String result = tool.generatePDF(
                "恋爱报告.pdf",
                "小草的恋爱分析报告",
                "根据您的描述，您目前处于恋爱状态，和伴侣的关系整体良好。\n" +
                        "以下是几点建议：\n" +
                        "1. 多花时间陪伴对方，增进感情交流。\n" +
                        "2. 学会倾听对方的想法和感受。\n" +
                        "3. 适当制造浪漫的小惊喜。\n" +
                        "4. 遇到矛盾时保持冷静，理性沟通。");
        assertNotNull(result);
        assertTrue(result.contains("PDF generated successfully"));
        System.out.println(result);
    }

    @Test
    public void testGeneratePDFEnglish() {
        String result = tool.generatePDF(
                "ai_report",
                "AI Agent Development Report",
                "This report covers the development of AI tools.\n" +
                        "Tools developed include: Web Search, Web Scraping, Terminal Operations, Resource Download, and PDF Generation.\n" +
                        "Each tool is designed to be registered as a ToolCallback for use with Spring AI ChatClient.");
        assertNotNull(result);
        assertTrue(result.contains("PDF generated successfully"));
        assertTrue(result.contains(".pdf"));
        System.out.println(result);
    }

    @Test
    public void testGeneratePDFAutoSuffix() {
        String result = tool.generatePDF(
                "no_suffix_test",
                "自动添加后缀测试",
                "这个文件名没有 .pdf 后缀，工具应该自动添加。");
        assertNotNull(result);
        assertTrue(result.contains(".pdf"));
        System.out.println(result);
    }

    @Test
    public void testGeneratePDFLongContent() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 50; i++) {
            content.append("第").append(i).append("段：这是一段用于测试长文本PDF生成的内容，验证分页和排版效果。\n");
        }
        String result = tool.generatePDF(
                "长文本测试.pdf",
                "长文本分页测试报告",
                content.toString());
        assertNotNull(result);
        assertTrue(result.contains("PDF generated successfully"));
        System.out.println(result);
    }
}
