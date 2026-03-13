package com.grass.grassaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.grass.grassaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @description: 文件操作工具类
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10 16:59
 */
public class FileOperationTool {

    private static final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/files";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file:" + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to" + filePath;
        } catch (Exception e) {
            return "Error writing file:" + e.getMessage();
        }
    }
}
