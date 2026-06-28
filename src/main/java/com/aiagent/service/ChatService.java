package com.aiagent.service;

import com.aiagent.config.RagConfig;
import com.aiagent.entity.ChatMessage;
import com.aiagent.entity.Conversation;
import com.aiagent.mapper.ChatMessageMapper;
import com.aiagent.mapper.ConversationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final ContentRetriever contentRetriever;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    /**
     * 创建新对话
     */
    public Conversation createConversation(Long userId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * 获取用户的对话列表
     */
    public List<Conversation> getConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getUpdatedAt)
        );
    }

    /**
     * 获取对话历史
     */
    public List<ChatMessage> getChatHistory(Long conversationId) {
        return chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByAsc(ChatMessage::getCreatedAt)
        );
    }

    /**
     * 发送消息并获取 AI 回复
     */
    public String chat(Long userId, Long conversationId, String userMessage) {
        // 1. 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("USER");
        userMsg.setContent(userMessage);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(userMsg);

        // 2. 构建带 RAG 的助手
        RagConfig.KnowledgeAssistant assistant = AiServices.builder(RagConfig.KnowledgeAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();

        // 3. 获取 AI 回复（带知识库检索）
        String aiReply = assistant.chat(userMessage);

        // 4. 保存 AI 回复
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setConversationId(conversationId);
        aiMsg.setRole("ASSISTANT");
        aiMsg.setContent(aiReply);
        aiMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(aiMsg);

        // 5. 更新对话时间
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setUpdatedAt(LocalDateTime.now());
            // 如果是第一条消息，用用户消息作为标题
            if ("新对话".equals(conversation.getTitle())) {
                String title = userMessage.length() > 20 ?
                        userMessage.substring(0, 20) + "..." : userMessage;
                conversation.setTitle(title);
            }
            conversationMapper.updateById(conversation);
        }

        return aiReply;
    }

    /**
     * 删除对话
     */
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            // 删除消息
            chatMessageMapper.delete(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getConversationId, conversationId)
            );
            // 删除对话
            conversationMapper.deleteById(conversationId);
        }
    }
}
