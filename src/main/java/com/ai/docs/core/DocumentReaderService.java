package com.ai.docs.core;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;

@Service
@RequiredArgsConstructor
public class DocumentReaderService {

  public List<Document> readPdf(Resource resource) {
    return new ParagraphPdfDocumentReader(resource).read();
  }

  public List<Document> readOneHtml(String uri) {
    String forObject = new RestTemplate().getForObject(uri, String.class);
    return new JsoupDocumentReader(forObject).read();
  }

  public List<Document> scrape(String url) {

    List<String> navUrls = Jsoup.parse(url)
      .select("nav a")
      .stream()
      .map(element -> element.attr("abs:href"))
      .toList();

    return navUrls.stream()
      .map(this::readOneHtml)
      .flatMap(List::stream)
      .toList();
  }
}
