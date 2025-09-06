package com.lyh.liangaiagent.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helloworld")
public class HelloworldController {
  private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

  private final ChatClient dashScopeChatClient;

  public HelloworldController(ChatClient.Builder chatClientBuilder) {
    this.dashScopeChatClient = chatClientBuilder
        .defaultSystem(DEFAULT_PROMPT)
         // 实现 Chat Memory 的 Advisor
         // 在使用 Chat Memory 时，需要指定对话 ID，以便 Spring AI 处理上下文。
         .defaultAdvisors(
             new MessageChatMemoryAdvisor(new InMemoryChatMemory())
         )
         // 实现 Logger 的 Advisor
         .defaultAdvisors(
             new SimpleLoggerAdvisor()
         )
         // 设置 ChatClient 中 ChatModel 的 Options 参数
         .defaultOptions(
             DashScopeChatOptions.builder()
                 .withTopP(0.7)
                 .build()
         )
         .build();
   }

    @GetMapping("/simple/chat")
  public String simpleChat(String query) {
    return dashScopeChatClient.prompt(query).call().content();
  }
 }