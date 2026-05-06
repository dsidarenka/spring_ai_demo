package com.epam.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/multimodal")
public class MultiModalController {

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  @PostMapping("/vision")
  public String recognizeImage(@RequestParam("file") MultipartFile file) {
    var mimeType = MimeTypeUtils.parseMimeType(
        file.getContentType() != null ? file.getContentType() : "image/jpeg");

    EndpointContext.set("/multimodal/vision");
    try {
      return chatClientBuilder.build()
          .prompt()
          .user(u -> u.text("Describe in detail what you see in this image.")
              .media(mimeType, file.getResource()))
          .call()
          .content();
    } finally {
      EndpointContext.clear();
    }
  }
}
