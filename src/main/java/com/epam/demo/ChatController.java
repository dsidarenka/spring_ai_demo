package com.epam.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/chat")
class ChatController {

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  /**
   * Simple synchronous call returning LLM response as a String
   */
  @GetMapping("/simple")
  public String simpleChat(String message) {
    ChatClient chatClient = chatClientBuilder.build();

    return chatClient.prompt()
        .user(message)
        .call()
        .content();
  }

  @Value("classpath:system_prompt.txt")
  private Resource resource;

  private String systemPrompt;

  @PostConstruct
  public void init() throws IOException {
    systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
  }

  @GetMapping("/jokes")
  public String systemPromt(String message) {
    ChatClient chatClient = chatClientBuilder
        //.defaultSystem(systemPrompt)
        .build();

    return chatClient.prompt()
        .user(message)
        .system(systemPrompt)
        .call()
        .content();
  }

  /**
   * Structured output mapping to a Java Record
   */
  @GetMapping("/structured")
  public ActorDetails getActorDetails(String name) {
    ChatClient chatClient = chatClientBuilder.build();

    return chatClient.prompt()
        .user(u ->
            u.text("Provide details for the actor: {name}")
                .param("name", name))
        .call()
        .entity(ActorDetails.class);
  }

  public record ActorDetails(String name, String famousMovie, int birthYear) {
  }
}
