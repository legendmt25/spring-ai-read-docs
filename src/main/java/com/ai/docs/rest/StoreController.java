package com.ai.docs.rest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ai.docs.core.DocumentReaderService;
import com.ai.docs.core.VectorStoreLoader;
import com.ai.docs.rest.model.UrlStoreRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;

@Slf4j
@RequestMapping("/api/v1/store")
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final VectorStoreLoader vectorStoreLoader;
  private final DocumentReaderService documentReaderService;

  /**
   * Separate endpoints:
   *  - POST /api/v1/store (multipart/form-data) accepts a 'file' part (PDF)
   *  - POST /api/v1/store (application/json) accepts a JSON body { "url": "..." }
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> storeFile(@RequestPart(value = "file", required = false) MultipartFile file) {
    try {
      if (file == null || file.isEmpty()) {
        return ResponseEntity.badRequest().body("Missing 'file' multipart");
      }

      String contentType = file.getContentType();
      Resource resource = file.getResource();

      if (contentType != null && contentType.equals("application/pdf")) {
        List<Document> documents = documentReaderService.readPdf(resource);
        vectorStoreLoader.load(documents);
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.badRequest().body("Only PDF files are supported");
      }
    } catch (Exception ex) {
      return ResponseEntity.status(500).body("Error processing file upload: " + ex.getMessage());
    }
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> storeUrl(@RequestBody(required = false) UrlStoreRequest request) {
    try {
      if (request == null || request.getUrl() == null || request.getUrl().isEmpty()) {
        return ResponseEntity.badRequest().body("Missing JSON body with 'url'");
      }

      CompletableFuture.runAsync(() -> {
        List<Document> documents;
        try {
          documents = documentReaderService.scrape(request.getUrl());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        vectorStoreLoader.load(documents);
      });

      return ResponseEntity.ok().build();
    } catch (Exception ex) {
      return ResponseEntity.status(500).body("Error processing URL store request: " + ex.getMessage());
    }
  }
}
