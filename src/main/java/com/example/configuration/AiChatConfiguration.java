package com.example.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfiguration {

  @Bean
  public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
    PromptTemplate promptTemplate = PromptTemplate.builder()
      .template("You are a helpful assistant. Answer the following question: {{question}}")
      .build();

    return ChatClient.builder(ollamaChatModel)
      .defaultSystem(promptTemplate.getTemplate())
      .defaultAdvisors(new SimpleLoggerAdvisor())
      .build();
  }
}
