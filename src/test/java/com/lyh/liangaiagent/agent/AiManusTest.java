package com.lyh.liangaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/15 22:06
 */
@SpringBootTest
class AiManusTest {
    @Resource
    private AiManus aiManus;

    @Test
    void testAiManus() {
        String userPrompt = """
                My partner lives in YinZhou District, Ningbo, please help me find a suitable date location within 5 kilometers, \s
                                                    And combined with some online pictures, make a detailed dating plan, \s
                                                    And output in PDF format
                """;
        String result = aiManus.run(userPrompt);
        assertNotNull(result);
    }
}