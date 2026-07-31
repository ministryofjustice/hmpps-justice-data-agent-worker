package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptsResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.random.Random

class PromptResourceTest : IntegrationTestBase() {
  @Autowired private lateinit var promptRepository: PromptRepository
  @Autowired  private lateinit var promptVersionRepository: PromptVersionRepository
  private val promptKey = UUID.randomUUID().toString()
  private val createdBy = UUID.randomUUID()
  @BeforeEach
  fun setup() {
    webTestClient = webTestClient
      .mutate()
      .responseTimeout(Duration.ofMillis(30000))
      .build()

    webTestClient.post().uri("/v1/prompts")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        PromptRequest(
          promptKey,
          "AI SERVICE",
          createdBy,
          PromptVersionRequest(
            "TEST-MODEL-5",
            "Get json output of firstname and lastname.",
            DataGenerator.jsonRequestSchema,
            DataGenerator.jsonResponseSchema
          )
        )
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse
  }

  /*@AfterEach
  fun cleanup() {
    runBlocking {
      promptVersionRepository.deleteAll()
      promptRepository.deleteAll()

    }

  }*/
  @Test
  fun `create prompt`() {
    val response = webTestClient.post().uri("/v1/prompts")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        PromptRequest(
          UUID.randomUUID().toString(),
          "AI SERVICE",
          createdBy,
          PromptVersionRequest(
            "TEST-MODEL-5",
            "Get json output of firstname and lastname.",
            DataGenerator.jsonRequestSchema,
            DataGenerator.jsonResponseSchema
          )
        )
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse

    assertNotNull(response)
  }

  @Test
  fun updatePrompt() {
  }

  @Test
  fun `get prompts`() {
    /*val response = webTestClient.post().uri("/v1/prompts")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        PromptRequest(
          promptKey,
          "AI SERVICE",
          createdBy,
          PromptVersionRequest(
            "TEST-MODEL-5",
            "Get json output of firstname and lastname.",
            DataGenerator.jsonRequestSchema,
            DataGenerator.jsonResponseSchema
          )
        )
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isCreated
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse*/

    val res = webTestClient.get().uri("/v1/prompts")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<List<PromptsResponse>>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as List<PromptsResponse>
    assertNotNull(res)
    assertTrue { res.isNotEmpty() }
  }

  @Test
  fun `get Prompt by key`() {
    val res = webTestClient.get().uri("/v1/prompts/${promptKey}")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse
    assertNotNull(res)
  }

  @Test
  fun `get prompt by random key`() {
    val res = webTestClient.get().uri("/v1/prompts/${UUID.randomUUID()}")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as ErrorResponse
  }

  @Test
  fun `get prompt by key And version`() {
    val res = webTestClient.get().uri("/v1/prompts/${promptKey}/versions/1")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse
    assertNotNull(res)
  }

  @Test
  fun `get prompt by prompt key And random version`() {
    val res = webTestClient.get().uri("/v1/prompts/${UUID.randomUUID()}/versions/${Random.nextInt()}")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<ErrorResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as ErrorResponse
  }

}