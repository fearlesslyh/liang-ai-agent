package com.lyh.liangaiagent.controller;

import com.lyh.liangaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/16 19:36
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp  loveApp;
    @Resource
    private ChatModel  chatModel;
    @Resource
    private ToolCallback[] tools;
    @GetMapping("/love_app/chat/sse")
    public String chatWithApp(String message,String chatId){
        loveApp.doChat(message,chatId);
    }
}
