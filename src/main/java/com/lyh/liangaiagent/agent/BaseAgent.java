package com.lyh.liangaiagent.agent;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/14 20:55
 */

import cn.hutool.core.util.StrUtil;
import com.aliyuncs.utils.StringUtils;
import com.lyh.liangaiagent.agent.model.AgentState;
import com.lyh.liangaiagent.exception.BusinessException;
import com.lyh.liangaiagent.exception.ThrowUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.lyh.liangaiagent.exception.ErrorCode.OPERATION_ERROR;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // 对话记忆
    private List<Message> memories = new ArrayList<>();

    /**
     * 执行代理
     *
     * @param userInput 用户输入
     * @return 代理执行结果
     */
    public String run(String userInput) {
        // 1. 校验
        if (this.state != AgentState.IDLE) {
            throw new BusinessException(OPERATION_ERROR, "代理正在运行中");
        }
        if (StrUtil.isBlank(userInput)) {
            throw new BusinessException(OPERATION_ERROR, "用户输入不能为空");
        }
        // 更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        memories.add(new UserMessage(userInput));
        // 用于保存执行结果
        List<String> results = new ArrayList<>();
        //  2.执行步骤循环
        try {
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("{} 执行第{} / {}步", this.name, stepNumber, maxSteps);
                String stepResult = step();
                String result = "step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            if (currentStep >= maxSteps) {
                this.state = AgentState.FINISHED;
                log.info("{} 执行完成，共执行{}步", this.name, currentStep);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("{} 执行失败: {}", this.name, e.getMessage());
            throw new BusinessException(OPERATION_ERROR, "代理执行失败");
        } finally {
            // 3.清理资源
            this.clean();
        }

    }

    /**
     * 执行代理（流式输出）
     *
     * @param userInput 用户输入
     * @return SSE实例
     */
    public SseEmitter runStream(String userInput) {
        // 创建SseEmitter实例，设置较长的超时时间，5分钟
        SseEmitter sseEmitter = new SseEmitter(300000L);

        // 使用线程异步处理，避免阻塞主线程
        // 同步执行的话，需要等待多个step执行完成才能返回，无法流式输出
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 校验代理状态和用户输入
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误：无法从状态运行代理: " + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userInput)) {
                    sseEmitter.send("错误：不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }
                // 更改状态
                this.state = AgentState.RUNNING;
                // 记录消息上下文
                memories.add(new UserMessage(userInput));
//            // 用于保存执行结果
//            List<String> results = new ArrayList<>();
                //  2.执行步骤循环
                try {
                    for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                        // 记录当前步骤
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("{} 执行到第{} / {}步", this.name, stepNumber, maxSteps);
                        // 执行单个步骤
                        String stepResult = step();
                        String result = "step " + stepNumber + ": " + stepResult;
                        // 发送结果
                        sseEmitter.send(result);
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxSteps) {
                        this.state = AgentState.FINISHED;
                        sseEmitter.send("执行完成，达到最大步数 " + currentStep + "步");
                    }
                    //  正常完成
                    sseEmitter.complete();
                } catch (Exception e) {
                    this.state = AgentState.ERROR;
                    log.error("{} 执行失败: {}", this.name, e.getMessage());
                    try {
                        sseEmitter.send("错误：代理执行失败");
                        sseEmitter.complete();
                    } catch (Exception ex) {
                        sseEmitter.completeWithError(ex);
                    }
                } finally {
                    // 3.清理资源
                    this.clean();
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        });

        // 超时以及完成任务的回调处理
        sseEmitter.onTimeout(() -> {
            state = AgentState.ERROR;
            clean();
            log.warn("{} 执行超时", this.name);
        });
        sseEmitter.onCompletion(() -> {
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
            }
            clean();
            log.info("{} 执行完成", this.name);
        });
        return sseEmitter;
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void clean() {
        // 子类可以重写此方法进行清理
    }
}
