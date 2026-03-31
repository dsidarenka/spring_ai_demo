package com.epam.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/etl")
class EtlExampleController {

  @Autowired
  private VectorStore vectorStore;

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  /**
   * ETL pipeline: Extract raw text → Transform into chunks → Load embeddings into vector store.
   */
  @PostMapping("/ingest")
  public IngestResult ingest(String text) {
    // Extract
    List<Document> documents = List.of(new Document(text));

    // Transform
    List<Document> chunks = new TokenTextSplitter().apply(documents);

    // Load
    vectorStore.add(chunks);

    return new IngestResult(chunks.size());
  }

  /**
   * RAG query: retrieves relevant chunks from the vector store and answers using them as context.
   */
  @GetMapping("/ask")
  public String ask(String question) {
    return chatClientBuilder.build()
        .prompt()
        .user(question)
        .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .call()
        .content();
  }

  public record IngestResult(int chunks) {
  }
}
