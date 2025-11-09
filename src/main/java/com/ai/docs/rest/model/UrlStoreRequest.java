package com.ai.docs.rest.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UrlStoreRequest {
  private String url;
}
