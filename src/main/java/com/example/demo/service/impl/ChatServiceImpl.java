package com.example.demo.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import cn.hutool.json.JSONUtil;
import com.example.demo.model.dto.ChatRequestDTO;
import com.example.demo.model.entity.ChatRecord;
import com.example.demo.model.vo.ChatResponseVO;
import com.example.demo.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String CHAT_SESSION_PREFIX = "chat:session:";
    private static final long MAX_HISTORY_ROUNDS = 3L;

    private final ChatClient chatClient;
    private final StringRedisTemplate stringRedisTemplate;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           StringRedisTemplate stringRedisTemplate) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一名专业、友好、简洁的中文智能助手，请结合历史对话上下文，给出清晰准确的中文回答。")
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String message = requestDTO.getMessage();
        String redisKey = CHAT_SESSION_PREFIX + sessionId;

        List<String> records = stringRedisTemplate.opsForList().range(redisKey, -MAX_HISTORY_ROUNDS, -1);
        String historyText = buildHistoryText(records);

        String finalPrompt = """
                以下是历史对话：
                %s

                当前用户问题：
                %s
                """.formatted(historyText, message);

        String answer = chatClient.prompt(finalPrompt)
                .call()
                .content();

        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setSessionId(sessionId);
        chatRecord.setUserMessage(message);
        chatRecord.setAssistantMessage(answer);
        chatRecord.setCreateTime(LocalDateTime.now());

        stringRedisTemplate.opsForList().rightPush(redisKey, JSONUtil.toJsonStr(chatRecord));

        Long size = stringRedisTemplate.opsForList().size(redisKey);
        if (size != null && size > MAX_HISTORY_ROUNDS) {
            stringRedisTemplate.opsForList().trim(redisKey, size - MAX_HISTORY_ROUNDS, size - 1);
        }

        return new ChatResponseVO(message, answer);
    }

    private String buildHistoryText(List<String> records) {
        if (records == null || records.isEmpty()) {
            return "暂无历史对话";
        }

        List<String> lines = new ArrayList<>();
        for (String recordJson : records) {
            if (!StringUtils.hasText(recordJson)) {
                continue;
            }
            ChatRecord record = JSONUtil.toBean(recordJson, ChatRecord.class);
            lines.add("用户：" + record.getUserMessage());
            lines.add("助手：" + record.getAssistantMessage());
        }

        if (lines.isEmpty()) {
            return "暂无历史对话";
        }
        return String.join("\n", lines);
    }
}
