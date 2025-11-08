package com.ai.docs.rest.model;

import lombok.Builder;
import lombok.Getter;

import org.springframework.ai.chat.messages.Message;

@Builder
@Getter
public class ChatMessageResponse {
  private String conversationId;
  private Message message;
}
