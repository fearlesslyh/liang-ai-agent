package com.lyh.liangaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/11 19:13
 */
@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "星露谷物语";
        String result = webSearchTool.searchWeb(query);
        assertNotNull(result);
    }
}