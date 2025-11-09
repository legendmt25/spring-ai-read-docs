package com.ai.docs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ApiSpecJsonFileExtractorTest {

  @Autowired
  private MockMvc mockMvc;

  @Value("${extraction.api-spec.json:src/main/resources/api-specs/openapi.v1.json}")
  private String extractionPath;

  @Value("${springdoc.api-docs.path:/v3/api-docs}")
  private String apiDocJsonPath;

  @Test
  void extractApiSpecJsonFile() throws Exception {
    File file = new File(extractionPath);
    Path filePath = file.toPath();
    if (file.exists()) {
      Assertions.assertThat(file.isFile()).isTrue();
    } else {
      Path path = file.getParentFile().toPath();
      if (Files.notExists(path)) {
        Files.createDirectory(path);
      }
      if (Files.notExists(filePath)) {
        Files.createFile(file.toPath());
      }
    }

    if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
      mockMvc.perform(MockMvcRequestBuilders.get(apiDocJsonPath))
        .andDo(result -> Files.write(file.toPath(), result.getResponse().getContentAsString().getBytes()));
    }
  }
}
