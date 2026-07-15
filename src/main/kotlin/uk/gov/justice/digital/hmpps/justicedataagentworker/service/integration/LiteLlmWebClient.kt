package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class LiteLlmWebClient(
  private val hmppsLiteLLMRestWebClient: WebClient,
) {

  companion object {
    val LOG: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun liteLlmChatCompletion(chatCompletionRequest: ChatCompletionRequest): Map<String, Any> = hmppsLiteLLMRestWebClient.post()
    .uri("/chat/completions")
    .bodyValue(chatCompletionRequest)
    .retrieve()
    .bodyToMono<Map<String, Any>>()
    .onErrorResume { e ->
      e.printStackTrace()
      if (e is WebClientResponseException) {
        LOG.warn("error ", e)
      }
      LOG.error("error", e)
      Mono.error(e)
    }.blockOptional(Duration.ofSeconds(20000L)).get()
}
