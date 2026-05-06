package com.epam.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recursive")
class RecursiveAdvisorController {

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  @Autowired
  private WeatherTools weatherTools;

  @Autowired
  private TraceService traceService;

  /**
   * Drives the Thought → Action → Observation cycle itself,
   * iterating until the LLM produces a final answer with no remaining tool calls.
   * This advisor supports multi-step agentic reasoning across as many iterations
   * as needed, managing conversation history between iterations automatically.
   */
  @GetMapping("/chat")
  public String chat(String message) {
    var chatResponse = chatClientBuilder.build()
        .prompt()
        .user(message)
        .tools(weatherTools)
        .advisors(ToolCallAdvisor.builder().build())
        .call()
        .chatResponse();
    traceService.recordFromResponse("/recursive/chat", message, chatResponse);
    return chatResponse.getResult().getOutput().getText();
  }
}
