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

  @Autowired
  private TraceService traceService;

  @GetMapping("/chat")
  public String chat(@RequestParam String message) {
    var chatResponse = chatClientBuilder.build()
        .prompt()
        .user(message)
        .tools(weatherTools)
        .call()
        .chatResponse();
    traceService.recordFromResponse("/tools/chat", message, chatResponse);
    return chatResponse.getResult().getOutput().getText();
  }
}
