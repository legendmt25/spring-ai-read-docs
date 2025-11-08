package com.ai.docs.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfiguration {

  @Bean
  public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
    return QuestionAnswerAdvisor.builder(vectorStore).build();
  }

  @Bean
  public ChatClient defaultChatClient(ChatModel chatModel, ChatMemory chatMemory, QuestionAnswerAdvisor advisor) {

    return ChatClient.builder(chatModel)
      .defaultAdvisors(
        SimpleLoggerAdvisor.builder().build(),
        MessageChatMemoryAdvisor.builder(chatMemory).build(), advisor)
      .build();
  }
}
