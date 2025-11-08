package com.ai.docs.DocParser;

import jakarta.annotation.PreDestroy;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Map;

@Configuration
public class VectorStoreLoader {

    private final QdrantVectorStore vectorStore;

    List<Document> documents = List.of(
            new Document("691F6EBF-D478-4394-B6A3-678A72EAEA0F", "npm commands include, install, npm run, npm exec", Map.of("source", "npm")),
//            new Document("Vector databases are crucial for RAG architectures."),
            new Document("62DE482F-BDAA-4032-BF9C-FF291575B295", "Embeddings represent text in a numerical format.", Map.of(" topic", " Embeddings"))
    );

    public VectorStoreLoader(QdrantVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    void load() {
        vectorStore.add(documents);
    }

    @PreDestroy
    void destroy() {
        documents.forEach(document -> {
            vectorStore.delete(document.getId());
        });
    }
}
