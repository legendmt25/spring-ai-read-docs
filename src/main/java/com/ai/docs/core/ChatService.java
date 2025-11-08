package com.ai.docs.core;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatClient chatClient;
  private final ChatMemoryRepository chatMemoryRepository;

  public List<Message> getMessages(String conversationId) {
    return chatMemoryRepository.findByConversationId(conversationId);
  }

  private Message call(String conversationId, String userMessage) {
    ChatClient.CallResponseSpec call = chatClient.prompt()
      .user(userMessage)
      .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
      .call();

    return Optional.of(call)
      .map(ChatClient.CallResponseSpec::chatResponse)
      .map(ChatResponse::getResult)
      .map(Generation::getOutput)
      .orElse(null);
  }

  public Message chat(String conversationId, String userMessage) {
    return call(conversationId, userMessage);
  }
}
