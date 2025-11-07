package com.example.rest.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRequest {
    private String message;
}
