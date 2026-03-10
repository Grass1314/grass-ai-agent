package com.grass.grassaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.grass.grassaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * @description: 资源下载工具，从URL下载文件保存到本地
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class ResourceDownloadTool {

    private static final String DOWNLOAD_DIR = FileConstant.FILE_SAVE_DIR + "/downloads";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Tool(description = "Download a file from a URL and save it locally. Supports images, documents, archives, etc.")
    public String downloadResource(
            @ToolParam(description = "The URL of the resource to download") String url,
            @ToolParam(description = "File name to save as, e.g. 'report.pdf', 'image.png'") String fileName) {
        try {
            FileUtil.mkdir(DOWNLOAD_DIR);

            // 文件名安全检查
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            String filePath = DOWNLOAD_DIR + "/" + fileName;

            long fileSize = HttpUtil.downloadFile(url, new File(filePath));

            if (fileSize > MAX_FILE_SIZE) {
                FileUtil.del(filePath);
                return "Error: File size exceeds limit (100MB). Download cancelled.";
            }

            String readableSize = FileUtil.readableFileSize(fileSize);
            return "File downloaded successfully.\n" +
                    "  Path: " + filePath + "\n" +
                    "  Size: " + readableSize;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }

    @Tool(description = "Download multiple resources from URLs and save them locally with auto-generated file names")
    public String batchDownload(
            @ToolParam(description = "Comma-separated URLs to download") String urls) {
        try {
            FileUtil.mkdir(DOWNLOAD_DIR);
            String[] urlArray = urls.split(",");
            StringBuilder sb = new StringBuilder();
            sb.append("Batch download results:\n\n");

            int success = 0;
            int failed = 0;
            for (String url : urlArray) {
                url = url.trim();
                if (url.isEmpty()) continue;
                try {
                    String fileName = extractFileName(url);
                    String filePath = DOWNLOAD_DIR + "/" + fileName;
                    long fileSize = HttpUtil.downloadFile(url, new File(filePath));
                    sb.append("  [OK] ").append(fileName)
                            .append(" (").append(FileUtil.readableFileSize(fileSize)).append(")\n");
                    success++;
                } catch (Exception e) {
                    sb.append("  [FAIL] ").append(url).append(" - ").append(e.getMessage()).append("\n");
                    failed++;
                }
            }

            sb.append("\nTotal: ").append(success).append(" succeeded, ").append(failed).append(" failed");
            sb.append("\nSaved to: ").append(DOWNLOAD_DIR);
            return sb.toString();
        } catch (Exception e) {
            return "Error in batch download: " + e.getMessage();
        }
    }

    private String extractFileName(String url) {
        String path = url.split("\\?")[0];
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.isEmpty() || !name.contains(".")) {
            name = "download_" + System.currentTimeMillis();
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
