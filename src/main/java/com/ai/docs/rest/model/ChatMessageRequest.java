package com.ai.docs.rest.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageRequest {
    private String message;
}
