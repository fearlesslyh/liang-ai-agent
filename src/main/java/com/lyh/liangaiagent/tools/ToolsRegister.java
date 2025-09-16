package com.lyh.liangaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/11 20:30
 */
@Configuration
public class ToolsRegister {
    @Value("${search-api.api-key}")
    private String searchApiKey;

    /**
     * 注册并返回所有可用的工具回调数组
     * 该方法创建各种工具实例并将其包装为ToolCallback数组，供Spring AI框架使用
     *
     * @return ToolCallback[] 包含所有已注册工具的回调数组
     */
    @Bean
    public ToolCallback[] tools() {
        // 创建各类工具实例
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        TerminateTool terminateTool = new TerminateTool();

        // 将所有工具实例转换为ToolCallback数组并返回
        return ToolCallbacks.from(
                fileOperationTool, webScrapingTool, resourceDownloadTool, pdfGenerationTool, webSearchTool, terminalOperationTool, terminateTool
        );
    }
}
