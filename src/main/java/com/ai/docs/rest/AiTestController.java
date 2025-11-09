package com.ai.docs.rest;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui")
public class AiTestController {

    private final ChatClient uiChatClient;

    @Value("${ai.model.uiPrompt}")
    private String uiUserPrompt;

    public AiTestController(@Qualifier("uiChatClient") ChatClient uiChatClient) {
      this.uiChatClient = uiChatClient;
    }

    String generate() {
        return uiChatClient
                .prompt(uiUserPrompt)
                .call()
                .content();
    }

    @GetMapping
    public String test() {

      String generatedHtml = generate();
      int beginHtmlIndex = generatedHtml.indexOf("```html");
      int endHtmlIndex = generatedHtml.indexOf("```", beginHtmlIndex);

      return generatedHtml.substring(beginHtmlIndex, endHtmlIndex);
    }

    @GetMapping("/prompt")
    public String prompt(@RequestParam(required = false, name = "text", defaultValue = "") String text) {
        return uiChatClient.prompt()
                .user(text)
                .call()
                .content();
    }
}
