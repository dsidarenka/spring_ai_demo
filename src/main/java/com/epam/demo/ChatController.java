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

  @GetMapping("/simple")
  public String simpleChat(String message) {
    EndpointContext.set("/chat/simple");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(message)
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
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
    EndpointContext.set("/chat/jokes");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(message)
          .system(systemPrompt)
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
  }

  @GetMapping("/withprompt")
  public String chatWithPrompt(String message, String systemPrompt) {
    EndpointContext.set("/chat/withprompt");
    try {
      var spec = chatClientBuilder.build()
          .prompt()
          .user(message);
      if (systemPrompt != null && !systemPrompt.isBlank()) {
        spec = spec.system(systemPrompt);
      }
      return spec.call().content();
    } finally {
      EndpointContext.clear();
    }
  }

  @GetMapping("/structured")
  public ActorDetails getActorDetails(String name) {
    EndpointContext.set("/chat/structured");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(u -> u.text("Provide details for the actor: {name}").param("name", name))
          .call()
          .entity(ActorDetails.class);
    } finally {
      EndpointContext.clear();
    }
  }

  public record ActorDetails(String name, String famousMovie, int birthYear) {
  }
}
