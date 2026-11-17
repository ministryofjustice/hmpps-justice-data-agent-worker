package uk.gov.justice.digital.hmpps.justicedataagentworker.health

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository

@Component("aiGatewayHealthPing")
class AIGateWayHealthPing(
  @Qualifier("hmppsLiteLLMRestWebClient") private val webClient: WebClient,
  private val promptVersionRepository: PromptVersionRepository,
) : ReactiveHealthIndicator {
  override fun health(): Mono<Health> = Mono.just(runBlocking { checkLlmModelHealth() })

  private suspend fun checkLlmModelHealth(): Health {
    try {
      val body = getAiGatewayHealth()
      if (body.unHealthyCounts == 0) {
        return Health.up().build()
      }
      val models = promptVersionRepository.findDistinctModelNames()
      val unHealthyModels = mutableSetOf<String>()
      body.healthyEndpoints.forEach { unhealthy ->
        models.forEach { model ->
          if (unhealthy.model.contains(model)) {
            unHealthyModels.add(model)
          }
        }
      }
      if (unHealthyModels.isNotEmpty()) {
        return Health.down().withDetail("Models down", unHealthyModels).build()
      }
      return Health.up().build()
    } catch (e: Exception) {
      return Health.down().withDetail("body", e.message!!).build()
    }
  }

  private suspend fun getAiGatewayHealth(): AiGatewayHealth = webClient.get()
    .uri("/health")
    .retrieve()
    .bodyToMono(AiGatewayHealth::class.java)
    .onErrorResume(WebClientResponseException::class.java) {
      Mono.error { throw Exception("Exception getting health info, status code: ${it.statusCode} ") }
    }
    .blockOptional().orElseThrow()
}

data class AiGatewayHealth(
  @JsonProperty("healthy_endpoints")
  val healthyEndpoints: List<Endpoint>,
  @JsonProperty("unhealthy_endpoints")
  val unHealthyEndpoints: List<Endpoint>,
  @JsonProperty("healthy_count")
  val healthyCounts: Int,
  @JsonProperty("unhealthy_count")
  val unHealthyCounts: Int,
)
data class Endpoint(
  @JsonProperty("aws_region_name")
  val awsRegionName: String,
  @JsonProperty("custom_llm_provider")
  val customLlmProvider: String,
  @JsonProperty("model")
  val model: String,
  @JsonProperty("model_id")
  val modelId: String,
)
