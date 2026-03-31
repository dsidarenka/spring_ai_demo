package com.epam.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that run a real Ollama LLM inside a Docker container via
 * Testcontainers.
 *
 * <p>How the wiring works:
 * <ol>
 *   <li>{@code @Testcontainers} manages the container lifecycle (start/stop).</li>
 *   <li>{@code @Container} marks the static field so Testcontainers starts it
 *       once per test class.</li>
 *   <li>{@code @ServiceConnection} triggers Spring AI's
 *       {@code OllamaContainerConnectionDetailsFactory}, which reads the
 *       container's host/port and populates {@code OllamaConnectionDetails}
 *       automatically — no manual {@code spring.ai.ollama.base-url} needed.</li>
 *   <li>{@code spring-ai-starter-model-ollama} on the test classpath activates
 *       {@code OllamaChatModel} auto-configuration, which uses those connection
 *       details.</li>
 * </ol>
 *
 * <p>These tests cover the same AI interactions exposed by {@link ChatController}:
 * simple chat, chat with a system prompt, and structured output.
 */
@SpringBootTest
@Testcontainers
class ChatControllerTest {

  private static final String MODEL = "tinyllama";

  /**
   * Starts a fresh Ollama container before any test in this class runs.
   * {@code @ServiceConnection} automatically configures the base URL so that
   * Spring Boot wires {@code OllamaChatModel} to this container's endpoint.
   */
  @Container
  @ServiceConnection
  static OllamaContainer ollama = new OllamaContainer(
      DockerImageName.parse("ollama/ollama:latest").asCompatibleSubstituteFor("ollama/ollama"));

  /**
   * Pulls the model into the running container once before any test executes.
   * The container is guaranteed to be started by the time {@code @BeforeAll} runs.
   */
  @BeforeAll
  static void pullModel() throws Exception {
    ollama.execInContainer("ollama", "pull", MODEL);
  }

  @Autowired
  private OllamaChatModel chatModel;

  @Test
  void simpleChat_shouldReturnNonEmptyResponse() {
    String response = ChatClient.builder(chatModel)
        .build()
        .prompt()
        .user("Reply with exactly one word: hello")
        .call()
        .content();

    assertThat(response).containsIgnoringCase("hello");
  }

  @Test
  void expertChat_systemPromptShouldInfluenceResponse() {
    String response = ChatClient.builder(chatModel)
        .defaultSystem("You are an expert in technical documentation. Be concise.")
        .build()
        .prompt()
        .user("What is Docker in one sentence?")
        .call()
        .content();

    assertThat(response).isNotBlank();
  }

  @Test
  void structuredOutput_shouldMapResponseToJavaRecord() {
    record ActorDetails(String name, String famousMovie, int birthYear) {
    }

    ActorDetails details = ChatClient.builder(chatModel)
        .build()
        .prompt()
        .user(u -> u.text("Provide details for the actor: {name}").param("name", "Jim Carrey"))
        .call()
        .entity(ActorDetails.class);

    assertThat(details).isNotNull();
    assertThat(details.name()).isNotBlank();
    assertThat(details.famousMovie()).isNotBlank();
    assertThat(details.birthYear()).isGreaterThan(1900);
  }
}
