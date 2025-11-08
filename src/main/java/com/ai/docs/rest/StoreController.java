package com.ai.docs.rest;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ai.docs.core.DocumentReaderService;
import com.ai.docs.core.VectorStoreLoader;

@RequestMapping("/api/store")
@RestController
@RequiredArgsConstructor
public class StoreController {

  private final VectorStoreLoader vectorStoreLoader;
  private final DocumentReaderService documentReaderService;


  @PostMapping(consumes = "multipart/form-data")
  public void attachFile(@RequestPart MultipartFile file) {

    String contentType = file.getContentType();
    Resource resource = file.getResource();

    if (contentType.equals("application/pdf")) {
      List<Document> documents = documentReaderService.readPdf(resource);
      vectorStoreLoader.load(documents);
    }
  }

  @PostMapping(consumes = "application/json")
  public void scrapeUrl(String url) {

    List<Document> documents = documentReaderService.scrape(url);
    vectorStoreLoader.load(documents);
//    vectorStoreLoader.loadVectorStore();
  }


}
