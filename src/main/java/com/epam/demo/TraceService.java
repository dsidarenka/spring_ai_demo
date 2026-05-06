package com.epam.demo;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
class TraceService {

  private final List<TraceRecord> traces = new CopyOnWriteArrayList<>();

  /** Record a trace from a ChatResponse, extracting token usage automatically. */
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
    record(endpoint, userText, prompt, completion, total);
  }

  /** Record a trace with explicit token counts (use null when unavailable). */
  void record(String endpoint, String userText,
              Long promptTokens, Long completionTokens, Long totalTokens) {
    String text = userText != null && userText.length() > 120
        ? userText.substring(0, 120) + "…"
        : (userText != null ? userText : "");
    traces.add(new TraceRecord(
        UUID.randomUUID().toString(),
        System.currentTimeMillis(),
        endpoint,
        text,
        promptTokens,
        completionTokens,
        totalTokens
    ));
  }

  List<TraceRecord> getAll() {
    return List.copyOf(traces);
  }

  void clear() {
    traces.clear();
  }
}
