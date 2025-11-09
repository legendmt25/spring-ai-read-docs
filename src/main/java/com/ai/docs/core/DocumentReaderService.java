package com.ai.docs.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReaderService {

  private final HtmlLinkScrapper htmlLinkScrapper;

  public List<Document> readPdf(Resource resource) {
    return new PagePdfDocumentReader(resource).read();
  }

  public List<Document> readHtml(Resource resource) {
    return new JsoupDocumentReader(resource).read();
  }

  public List<Document> scrape(String url) throws IOException {
    List<Document> documents = scrape(url, url, new HashSet<>());
    log.info("Scraping from url: {} done", url);
    return documents;
  }

  public List<Document> scrape(String url, String baseUrl, Set<String> visited) throws IOException {

    List<Document> allDocuments = new ArrayList<>();

    if (visited.contains(url)) {
      return allDocuments;
    }

    log.info("Scrape url: {}", url);
    visited.add(url);

    Connection.Response jsoupResponse;
    try {
      jsoupResponse = Jsoup.connect(url).execute();
    } catch (IOException e) {
      log.warn("Failed to fetch url: {} with exception {}", url, e.getMessage());
      return allDocuments;
    }

    if (jsoupResponse.statusCode() != HttpStatus.OK.value()) {
      log.warn("Failed to fetch url: {} with status code {}, {}", url, jsoupResponse.statusCode(), jsoupResponse.statusMessage());
      return allDocuments;
    }

    org.jsoup.nodes.Document document = jsoupResponse.parse();
    List<String> links = htmlLinkScrapper.scrapeLinksStream(document)
      .filter(link -> link.startsWith(baseUrl))
      .filter(Predicate.not(visited::contains))
      .toList();

    Resource resource = new ByteArrayResource(document.toString().getBytes());
    List<Document> docs = readHtml(resource);

    for (String link : links) {
      allDocuments.addAll(scrape(link, baseUrl, visited));
    }

    allDocuments.addAll(docs);
    return allDocuments;
  }
}
