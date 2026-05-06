package com.epam.demo;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
class TraceService {

  private final List<TraceRecord> traces = new CopyOnWriteArrayList<>();

  void recordFromResponse(String endpoint, String userText, ChatResponse chatResponse) {
    Long prompt = null, completion = null, total = null;
    try {
      var usage = chatResponse.getMetadata().getUsage();
      if (usage != null) {
        prompt     = usage.getPromptTokens().longValue();
        total      = usage.getTotalTokens().longValue();
        completion = (prompt != null && total != null) ? total - prompt : null;
      }
    } catch (Exception ignored) {}

    String text = userText != null && userText.length() > 120
        ? userText.substring(0, 120) + "…"
        : (userText != null ? userText : "");

    traces.add(new TraceRecord(
        UUID.randomUUID().toString(),
        System.currentTimeMillis(),
        endpoint,
        text,
        prompt,
        completion,
        total
    ));
  }

  List<TraceRecord> getAll() {
    return List.copyOf(traces);
  }

  void clear() {
    traces.clear();
  }
}
