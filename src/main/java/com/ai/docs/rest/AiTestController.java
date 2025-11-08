package com.ai.docs.rest;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/testing")
@RequiredArgsConstructor
public class AiTestController {

    private final ChatClient chatClient;

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
