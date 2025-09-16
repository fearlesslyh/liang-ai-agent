package com.lyh.liangaiagent.agent;

import com.lyh.liangaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/9/14 20:57
 */

@Component
public class AiManus extends ToolCallAgent{
    public AiManus(ToolCallback[] tools, ChatModel dashScopeChatModel) {
        super(tools);
        setName("AiManus");
        String SYSTEM_PROMPT = """  
                You are AIManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        setMaxSteps(20);
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        setChatClient(chatClient);
    }
}
