package com.grass.grasspdftoimagemcpserver.tool;

import com.grass.grasspdftoimagemcpserver.service.PdfToImageService;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class PdfToImageMcpTool {

    @Resource
    private PdfToImageService pdfToImageService;

    @Tool(description = """
            将 PDF 文件按页转换为图片，返回图片格式为 Base64，供大模型进行图片文字识别（OCR）。
            返回每页图片，大模型可直接识别图片中的文字、表格、图表等内容。
            """)
    public List<McpSchema.Content> pdfToImagesForOcr(
            @ToolParam(description = "PDF 文件的绝对路径") String pdfPath,
            @ToolParam(description = "输出分辨率（DPI），默认 200，建议范围 72-300") int dpi
    ) {
        try {
            // 设置dpi默认值为200
            dpi = dpi > 0 ? dpi : 200;
            List<PdfToImageService.PageImage> pages =
                    pdfToImageService.convertToImages(pdfPath, dpi);

            return getContentList(pages);

        } catch (IllegalArgumentException e) {
            return List.of(new McpSchema.TextContent("参数错误：" + e.getMessage()));
        } catch (Exception e) {
            return List.of(new McpSchema.TextContent("转换失败：" + e.getMessage()));
        }
    }

    @NonNull
    private static List<McpSchema.Content> getContentList(List<PdfToImageService.PageImage> pages) {
        List<McpSchema.Content> contents = new ArrayList<>();

        for (PdfToImageService.PageImage page : pages) {
            // 页码说明文字
            contents.add(new McpSchema.TextContent(
                    String.format("第 %d 页（共 %d 页）：", page.pageNumber(), page.totalPages())
            ));
            // 图片内容供模型视觉识别
            contents.add(new McpSchema.ImageContent(
                    null, null, page.base64(), page.mimeType()
            ));
        }
        return contents;
    }

    /**
     * 将 PDF 转成图片，并保存到指定目录，返回保存图片的路径列表
     *
     * @param pdfPath      PDF 文件路径（绝对路径）
     * @param outputDir    图片保存目录（绝对路径）。目录不存在会尝试创建。
     * @param dpi          渲染分辨率，建议 200
     * @return 图片文件绝对路径列表，错误时返回单个 TextContent 路径描述错误信息
     */
    @Tool(description = """
            将 PDF 文件按页渲染为 PNG 图片并保存到指定目录。
            返回所有图片的文件路径列表，方便后续业务使用本地图片文件。
            """)
    public List<McpSchema.Content> pdfToImagesAndSave(
            @ToolParam(description = "PDF 文件绝对路径") String pdfPath,
            @ToolParam(description = "图片保存目录绝对路径") String outputDir,
            @ToolParam(description = "渲染 DPI，推荐 200") int dpi
    ) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return List.of(new McpSchema.TextContent("创建图片保存目录失败: " + outputDir));
                }
            }
            if (!dir.isDirectory()) {
                return List.of(new McpSchema.TextContent("指定路径不是目录: " + outputDir));
            }

            // 设置dpi默认值为200
            dpi = dpi > 0 ? dpi : 200;
            List<PdfToImageService.PageImage> pages = pdfToImageService.convertToImages(pdfPath, dpi);

            List<McpSchema.Content> result = new ArrayList<>();

            for (PdfToImageService.PageImage page : pages) {
                // 图片文件名格式：时间毫秒值_第{页码}_共{总页数}.png
                String fileName = String.format("%d_page_%d_of_%d.png", System.currentTimeMillis(), page.pageNumber(), page.totalPages());
                File imageFile = new File(dir, fileName);

                // base64 解码保存为文件
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    byte[] imageBytes = Base64.getDecoder().decode(page.base64());
                    fos.write(imageBytes);
                }

                // 返回图片的绝对路径给调用方
                result.add(new McpSchema.TextContent(imageFile.getAbsolutePath()));
            }

            return result;

        } catch (IllegalArgumentException e) {
            return List.of(new McpSchema.TextContent("参数错误：" + e.getMessage()));
        } catch (IOException e) {
            return List.of(new McpSchema.TextContent("IO 异常：" + e.getMessage()));
        } catch (Exception e) {
            return List.of(new McpSchema.TextContent("PDF 转图片并保存失败：" + e.getMessage()));
        }
    }
}

