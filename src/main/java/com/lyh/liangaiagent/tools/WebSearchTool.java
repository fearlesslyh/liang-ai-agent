package com.lyh.liangaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "通过Google搜索引擎搜索内容")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 创建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "google");
        try {
            // 通过 Hutool 工具包发起 HTTP 请求，并获取返回结果
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            // 把返回结果转换为 JSON 对象
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取 organic_results 部分
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            // 取出返回结果的前 10 条
            List<Object> objects = organicResults.subList(0, 5);
            // 拼接搜索结果为字符串
            String result = objects.stream()
                    //  将每个对象转换为 JSON 对象
                    .map(obj -> {
                        JSONObject tmpJSONObject = (JSONObject) obj;
                        return tmpJSONObject.toString();
                    })
                    // 拼接为字符串
                    .collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            return "Error searching Google: " + e.getMessage();
        }
    }
}
