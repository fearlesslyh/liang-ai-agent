package com.lyh.liangaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 恋爱应用上下文查询增强器工厂类
 * 用于创建专门处理恋爱相关问题的上下文查询增强器实例
 */
public class LoveAppContextualQueryAugmenterFactory {
    /**
     * 创建上下文查询增强器实例
     * 该实例配置为只处理恋爱相关问题，当上下文为空时会返回预定义的提示信息
     *
     * @return ContextualQueryAugmenter 上下文查询增强器实例
     */
    public static ContextualQueryAugmenter createInstance() {
        // 创建空上下文时的提示模板，用于告知用户只能回答恋爱相关问题
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}

