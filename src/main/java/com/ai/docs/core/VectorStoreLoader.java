package com.ai.docs.core;

import java.util.List;

import jakarta.annotation.PreDestroy;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class VectorStoreLoader {

    private final VectorStore vectorStore;

//    List<Document> documents = List.of(
//      new Document("691F6EBF-D478-4394-B6A3-678A72EAEA0F", "npm commands include, install, npm run, npm exec", Map.of("source", "npm")),
//            new Document("Vector databases are crucial for RAG architectures."),
//      new Document("62DE482F-BDAA-4032-BF9C-FF291575B295", "Embeddings represent text in a numerical format.", Map.of(" topic", " Embeddings"))
//    );

//    @EventListener(ApplicationReadyEvent.class)
    public void load(List<Document> documents) {
        vectorStore.add(documents);
    }

//    @PreDestroy
//    void destroy() {
//        vectorStore.delete("*");
//    }
}
