package com.ai.docs.DocParser;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Vector;

@RestController
@RequestMapping("/testing")
public class AiTestController {

    private final ChatClient chatClient;

    AiTestController(ChatClient.Builder chatClientBuilder, QuestionAnswerAdvisor advisor) {
        this.chatClient = chatClientBuilder.defaultAdvisors(advisor).build();
    }

    String generate() {

        return chatClient
                .prompt("Generate a start page html for a document parser app, please use regular html and css, js for interactivity, no js frameworks, dont give flavor text, just provide html, use tailwind cdn")
                .call()
                .content();
    }

    @GetMapping
    public String test() {
        return generate();
    }

    @GetMapping("/p")
    public String prompt(@RequestParam(required = false, name = "text", defaultValue = "") String text) {
        return chatClient.prompt()
                .user(text)
                .call()
                .content();
    }
}
