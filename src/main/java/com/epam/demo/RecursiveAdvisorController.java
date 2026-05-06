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

  @GetMapping("/chat")
  public String chat(String message) {
    EndpointContext.set("/recursive/chat");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(message)
          .tools(weatherTools)
          .advisors(ToolCallAdvisor.builder().build())
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
  }
}
