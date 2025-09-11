package com.lyh.liangaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.lyh.liangaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "读取文件")
    public String readFile(@ToolParam(description = "读取的文件名") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "读取文件失败" + e.getMessage();
        }
    }

    @Tool(description = "写入文件")
    public String writeFile(@ToolParam(description = "写入的文件名") String fileName, @ToolParam(description = "写入的文件内容") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "写入文件成功" + filePath;
        } catch (Exception e) {
            return "写入文件失败" + e.getMessage();
        }
    }
}
