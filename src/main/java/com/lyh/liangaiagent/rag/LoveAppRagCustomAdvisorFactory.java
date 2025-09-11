package com.lyh.liangaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

@Slf4j
public class LoveAppRagCustomAdvisorFactory {
    /**
     * 创建自定义RAG增强顾问
     * 该方法根据向量存储和状态条件创建一个检索增强生成顾问，
     * 用于在聊天过程中检索相关的文档信息来增强AI的回答
     *
     * @param vectorStore 向量存储实例，用于存储和检索向量化的文档数据
     * @param status      状态过滤条件，用于筛选具有特定状态的文档
     * @return 返回配置好的检索增强顾问实例
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 构建过滤表达式，筛选指定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("love_status", status)
                .build();

        // 创建文档检索器，配置向量存储、过滤条件、相似度阈值和返回文档数量
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.5) // 相似度阈值
                .topK(3) // 返回文档数量
                .build();

        // 构建并返回检索增强顾问
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}



