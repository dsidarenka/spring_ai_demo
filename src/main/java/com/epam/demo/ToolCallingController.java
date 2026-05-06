package com.epam.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tools")
class ToolCallingController {

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  @Autowired
  private WeatherTools weatherTools;

  @GetMapping("/chat")
  public String chat(@RequestParam String message) {
    EndpointContext.set("/tools/chat");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(message)
          .tools(weatherTools)
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
  }
}
