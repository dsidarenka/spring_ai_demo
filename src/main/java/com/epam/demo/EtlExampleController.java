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

  @PostMapping("/ingest")
  public IngestResult ingest(String text) {
    List<Document> documents = List.of(new Document(text));
    List<Document> chunks = new TokenTextSplitter().apply(documents);
    vectorStore.add(chunks);
    return new IngestResult(chunks.size());
  }

  @GetMapping("/ask")
  public String ask(String question) {
    EndpointContext.set("/etl/ask");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(question)
          .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
  }

  public record IngestResult(int chunks) {
  }
}
