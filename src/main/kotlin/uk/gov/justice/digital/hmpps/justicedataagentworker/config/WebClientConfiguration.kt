package uk.gov.justice.digital.hmpps.justicedataagentworker.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @param:Value("\${hmpps.auth.base-url}") val hmppsAuthBaseUri: String,
  @param:Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @param:Value("\${api.timeout:20s}") val timeout: Duration,
  @param:Value("\${hmpps.ai-gateway.api-key}") val apiKey: String,
  @param:Value("\${hmpps.ai-gateway.base-url}") val llmBaseUrl: String,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun hmppsLiteLLMRestWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl(llmBaseUrl).defaultHeader("Authorization", "Bearer $apiKey").build()
}
