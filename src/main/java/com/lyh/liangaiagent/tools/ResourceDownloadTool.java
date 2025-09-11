package com.lyh.liangaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.lyh.liangaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class ResourceDownloadTool {

    //
    @Tool(description = "从URL下载资源")
    public String downloadResource(@ToolParam(description = "下载资源的URL地址") String url, @ToolParam(description = "下载资源的文件名") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download/";
        String filePath = fileDir + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 Hutool 的 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "下载成功: " + filePath;
        } catch (Exception e) {
            return "下载失败: " + e.getMessage();
        }
    }
}
