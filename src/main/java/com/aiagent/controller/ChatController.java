package com.aiagent.controller;

import com.aiagent.dto.ApiResponse;
import com.aiagent.dto.ChatRequest;
import com.aiagent.entity.ChatMessage;
import com.aiagent.entity.Conversation;
import com.aiagent.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ApiResponse<Conversation> createConversation(HttpServletRequest request,
                                                         @RequestBody(required = false) Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String title = body != null ? body.get("title") : null;
        Conversation conversation = chatService.createConversation(userId, title);
        return ApiResponse.success(conversation);
    }

    @GetMapping("/conversations")
    public ApiResponse<List<Conversation>> getConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Conversation> conversations = chatService.getConversations(userId);
        return ApiResponse.success(conversations);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<ChatMessage>> getChatHistory(@PathVariable Long conversationId) {
        List<ChatMessage> messages = chatService.getChatHistory(conversationId);
        return ApiResponse.success(messages);
    }

    @PostMapping("/send")
    public ApiResponse<Map<String, String>> sendMessage(HttpServletRequest request,
                                                          @RequestBody ChatRequest chatRequest) {
        Long userId = (Long) request.getAttribute("userId");
        String reply = chatService.chat(userId, chatRequest.getConversationId(), chatRequest.getMessage());
        return ApiResponse.success(Map.of("reply", reply));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Void> deleteConversation(HttpServletRequest request,
                                                  @PathVariable Long conversationId) {
        Long userId = (Long) request.getAttribute("userId");
        chatService.deleteConversation(conversationId, userId);
        return ApiResponse.success();
    }
}
