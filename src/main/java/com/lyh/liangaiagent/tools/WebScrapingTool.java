package com.lyh.liangaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    @Tool(description = "抓取网页内容")
    public String scrapeWeb(@ToolParam(description = "抓取的网页地址") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "抓取网页失败: " + e.getMessage();
        }
    }
}
