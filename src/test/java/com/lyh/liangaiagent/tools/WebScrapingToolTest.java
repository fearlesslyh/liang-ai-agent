package com.lyh.liangaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/11 19:32
 */
@SpringBootTest
class WebScrapingToolTest {

    @Test
    void scrapeWeb() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.bilibili.com/";
        String result = webScrapingTool.scrapeWeb(url);
        assertNotNull(result);
    }
}