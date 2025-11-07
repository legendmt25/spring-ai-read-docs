package com.example.rest;

import com.example.rest.model.ChatRequest;

import jakarta.servlet.http.HttpSession;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatClient chatClient;

  @PostMapping
  public ChatResponse chat(@RequestBody ChatRequest request, HttpSession session) {

    List<Message> messages = session.getAttribute("messages") == null
      ? new ArrayList<>()
      : (List<Message>) session.getAttribute("messages");

    ChatResponse response = chatClient.prompt()
      .user(request.getMessage())
      .messages(messages)
      .call()
      .chatClientResponse().chatResponse();

    AssistantMessage message = Optional.ofNullable(response)
      .map(ChatResponse::getResult)
      .map(Generation::getOutput)
      .orElseThrow();

    messages.add(message);
    return response;
  }
}
