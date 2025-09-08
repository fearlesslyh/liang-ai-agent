package com.lyh.liangaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @date 2025/9/6 19:53
 */
@SpringBootTest
public class LoveAppPGVectorConfigTest {
    @Resource
    private VectorStore pgVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(
                new Document("能不能成功", Map.of("meta1", "meta1")),
                new Document("给我过！"),
                new Document("跨越山海，与爱同行。", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        pgVectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = this.pgVectorStore.similaritySearch(SearchRequest.builder().query("能不能成功").topK(5).build());
        Assertions.assertNotNull(results);
    }
}