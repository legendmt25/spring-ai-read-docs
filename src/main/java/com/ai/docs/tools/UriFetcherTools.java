package com.ai.docs.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.web.client.RestTemplate;

public class UriFetcherTools {

  @Tool(description = "Fetches data from a given URL and returns the content as a string.")
  public String fetchData(String uri) {
    return new RestTemplate().getForObject(uri, String.class);
  }
}
