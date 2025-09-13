package com.lyh.liangaiagent.app;

import com.lyh.liangaiagent.advisor.MyLoggerAdvisor;
import com.lyh.liangaiagent.chatmemory.FileBasedChatMemory;
import com.lyh.liangaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * LoveApp 类是恋爱心理专家聊天应用的核心实现类，提供多种聊天交互方式。
 * 支持普通聊天、结构化输出（生成恋爱报告）、结合 RAG 检索增强的聊天以及集成工具调用的聊天功能。
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    /**
     * 系统提示词，定义了聊天机器人扮演的角色和交互流程：
     * 以恋爱心理专家身份引导用户倾诉恋爱难题，并根据用户所处的状态（单身、恋爱、已婚）提出针对性问题，
     * 最终给出个性化建议。
     */
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 构造函数，初始化 ChatClient 实例。
     * 使用基于文件的对话记忆存储机制，并配置默认系统提示和基础记忆顾问。
     *
     * @param dashscopeChatModel 聊天模型实例，用于构建 ChatClient
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    /**
     * 执行一次普通的聊天交互。
     * 根据用户输入的消息和会话 ID 返回 AI 回复内容。
     *
     * @param message 用户发送的消息内容
     * @param chatId  当前会话标识符，用于区分不同用户的上下文记忆
     * @return AI 回复的文本内容
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 定义恋爱报告的结构，包含标题和建议列表。
     *
     * @param title       报告标题
     * @param suggestions 建议项列表
     */
    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 执行一次结构化输出的聊天交互，要求 AI 返回特定格式的恋爱报告。
     *
     * @param message 用户发送的消息内容
     * @param chatId  当前会话标识符，用于管理上下文记忆
     * @return 包含标题和建议列表的恋爱报告对象
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private VectorStore pgVectorStore;
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 执行一次带有检索增强（RAG）能力的聊天交互。
     * 首先对用户输入进行查询改写，然后使用改写后的查询结合本地知识库进行问答增强。
     *
     * @param message 用户发送的消息内容
     * @param chatId  当前会话标识符，用于维护上下文记忆
     * @return AI 回复的文本内容
     */
    public String doChatWithRag(String message, String chatId) {
        // 使用改写后的查询提升匹配效果
        String rewriter = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewriter)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用本地知识库，基于 PGVector 问答
                .advisors(new QuestionAnswerAdvisor(pgVectorStore))
                // 应用本地知识库 (注释掉的部分)
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用云知识库问答 (注释掉的部分)
//                .advisors(loveAppRagCloudAdvisor)
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "单身"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] tools;

    /**
     * 执行一次支持工具调用的聊天交互。
     * 允许 AI 在响应过程中调用预定义的工具来辅助生成答案。
     *
     * @param message 用户发送的消息内容
     * @param chatId  当前会话标识符，用于上下文记忆管理
     * @return AI 回复的文本内容
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(tools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // ai 调用 MCP 服务
    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
