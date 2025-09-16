package com.lyh.liangaiagent.controller;

import com.lyh.liangaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * AI功能接口控制器，提供同步与流式对话服务
 *
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/16 19:36
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;
    @Resource
    private ChatModel chatModel;
    @Resource
    private ToolCallback[] tools;

    /**
     * 处理同步聊天请求，返回即时响应结果
     *
     * @param message 用户输入的聊天内容
     * @param chatId  对话唯一标识符，用于维护会话上下文
     * @return 完整的聊天响应内容字符串
     */
    @GetMapping("/love_app/chat/sync")
    public String chatWithSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

//      Flux 是 响应式流（Reactive Stream）的核心实现：
//      异步流式处理：数据按需生成/消费（类似生产者-消费者模型）
//      背压支持：自动调节数据流速防止内存溢出
//      事件驱动：基于发布-订阅模式（非内存存储结构）

    /**
     * 处理SSE流式聊天请求，分块返回响应数据
     *
     * @param message 用户输入的聊天内容
     * @param chatId  对话唯一标识符，用于维护会话上下文
     * @return 响应式流，按数据块逐步发送聊天响应
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithSseOption1(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 处理SSE流式（ServerSentEvent）聊天请求，分块返回响应数据
     *
     * @param message 用户输入的聊天内容
     * @param chatId  对话唯一标识符，用于维护会话上下文
     * @return 响应式流，按数据块逐步发送聊天响应
     */
    @GetMapping(value = "/love_app/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithSseOption2(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    /**
     * 基于SseEmitter实现流式聊天响应，通过服务器发送事件(SSE)向客户端推送分块消息
     *
     * @param message 用户输入的聊天内容
     * @param chatId  对话唯一标识符，用于维护会话上下文
     * @return SseEmitter 对象，建立服务器推送事件连接
     * <p>
     * 工作流程：
     * 1. 创建长生命周期SSE连接（3分钟超时）
     * 2. 订阅AI流式响应数据流
     * 3. 实时推送消息片段至客户端
     * 4. 自动处理完成/错误状态
     */
    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建超时时间为3分钟的SSE连接器
        SseEmitter emitter = new SseEmitter(180000L);
        // 订阅AI流式响应并建立推送管道
        loveApp.doChatByStream(message, chatId)
                // 核心处理逻辑：将每个数据块通过SSE推送至客户端。subscribe() 方法用于订阅流式数据
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

}
