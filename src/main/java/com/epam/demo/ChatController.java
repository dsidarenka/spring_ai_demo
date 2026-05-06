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

  @Autowired
  private TraceService traceService;

  /**
   * Simple synchronous call returning LLM response as a String
   */
  @GetMapping("/simple")
  public String simpleChat(String message) {
    var chatResponse = chatClientBuilder.build()
        .prompt()
        .user(message)
        .call()
        .chatResponse();
    traceService.recordFromResponse("/chat/simple", message, chatResponse);
    return chatResponse.getResult().getOutput().getText();
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
    var chatResponse = chatClientBuilder.build()
        .prompt()
        .user(message)
        .system(systemPrompt)
        .call()
        .chatResponse();
    traceService.recordFromResponse("/chat/jokes", message, chatResponse);
    return chatResponse.getResult().getOutput().getText();
  }

  @GetMapping("/withprompt")
  public String chatWithPrompt(String message, String systemPrompt) {
    var spec = chatClientBuilder.build()
        .prompt()
        .user(message);
    if (systemPrompt != null && !systemPrompt.isBlank()) {
      spec = spec.system(systemPrompt);
    }
    var chatResponse = spec.call().chatResponse();
    traceService.recordFromResponse("/chat/withprompt", message, chatResponse);
    return chatResponse.getResult().getOutput().getText();
  }

  /**
   * Structured output — uses .entity() so ChatResponse isn't accessible here;
   * the trace is still recorded (without token counts) so the request appears in the list.
   */
  @GetMapping("/structured")
  public ActorDetails getActorDetails(String name) {
    traceService.record("/chat/structured", "actor: " + name, null, null, null);
    return chatClientBuilder.build()
        .prompt()
        .user(u -> u.text("Provide details for the actor: {name}").param("name", name))
        .call()
        .entity(ActorDetails.class);
  }

  public record ActorDetails(String name, String famousMovie, int birthYear) {
  }
}
