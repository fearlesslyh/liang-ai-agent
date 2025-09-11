package com.lyh.liangaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/11 20:00
 */
@SpringBootTest
class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String url = "https://i1.hdslb.com/bfs/face/321b61d25bf4f23ff3114678966ff8f2ee52d2c0.jpg@240w_240h_1c_1s_!web-avatar-nav.avif";
        String fileName = "曼彻斯顿.jpg";
        String result = resourceDownloadTool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}