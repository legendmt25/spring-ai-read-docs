package com.ai.docs.core;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Component
public class HtmlLinkScrapper {

  public Set<String> scrapeLinks(String html) {
    return scrapeLinksStream(html).collect(Collectors.toSet());
  }

  public Stream<String> scrapeLinksStream(Document document) {
    return document.selectStream("nav a")
      .map(element -> element.attr("abs:href"))
      .map(link -> link.replaceAll("#.*$", ""))
      .filter(StringUtils::hasText)
      .distinct();
  }

  public Stream<String> scrapeLinksStream(String html) {
    return scrapeLinksStream(Jsoup.parse(html));
  }

  public Stream<String> fetchAndScrapeLinksStream(String url) throws IOException {
    return scrapeLinksStream(Jsoup.connect(url).get());
  }
}
