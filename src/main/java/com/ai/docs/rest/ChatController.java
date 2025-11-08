package com.ai.docs.rest;

import com.ai.docs.core.ChatService;
import com.ai.docs.rest.model.ChatMessageRequest;
import com.ai.docs.rest.model.ChatMessageResponse;

import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @GetMapping("/{conversationId}")
  public List<Message> getMessages(@PathVariable String conversationId) {
    return chatService.getMessages(conversationId);
  }

  @PostMapping("/{conversationId}")
  public ChatMessageResponse chat(@PathVariable String conversationId, @RequestBody ChatMessageRequest request) {

    Message responseMessage = chatService.chat(conversationId, request.getMessage());
    return ChatMessageResponse.builder()
      .conversationId(conversationId)
      .message(responseMessage)
      .build();
  }

  @PostMapping
  public ChatMessageResponse chat(@RequestBody ChatMessageRequest request) {

    String conversationId = UUID.randomUUID().toString();
    Message responseMessage = chatService.chat(conversationId, request.getMessage());
    return ChatMessageResponse.builder()
      .conversationId(conversationId)
      .message(responseMessage)
      .build();
  }
}
