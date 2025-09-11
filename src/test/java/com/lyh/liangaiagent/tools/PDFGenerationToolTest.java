package com.lyh.liangaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/11 20:26
 */
@SpringBootTest
class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String content="曼彻斯顿大帅哥";
        String fileName="曼彻斯顿.pdf";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}