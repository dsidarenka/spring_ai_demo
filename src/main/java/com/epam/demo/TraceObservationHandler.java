package com.epam.demo;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Fires on every individual ChatModel.call() — including each intermediate round-trip
 * inside a tool-calling loop — and records a trace entry for each one.
 * Spring Boot auto-registers all ObservationHandler beans with the ObservationRegistry.
 */
@Component
class TraceObservationHandler implements ObservationHandler<ChatModelObservationContext> {

  @Autowired
  private TraceService traceService;

  @Override
  public void onStop(ChatModelObservationContext context) {
    traceService.recordFromResponse(
        EndpointContext.get(),
        extractUserText(context),
        context.getResponse()
    );
  }

  private String extractUserText(ChatModelObservationContext context) {
    try {
      return context.getRequest().getInstructions().stream()
          .filter(msg -> msg.getMessageType() == MessageType.USER)
          .findFirst()
          .map(msg -> msg.getText())
          .orElse("");
    } catch (Exception ignored) {
      return "";
    }
  }

  @Override
  public boolean supportsContext(Observation.Context context) {
    return context instanceof ChatModelObservationContext;
  }
}
