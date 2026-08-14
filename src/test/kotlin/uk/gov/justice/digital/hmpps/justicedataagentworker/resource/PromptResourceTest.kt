package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptsResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

class PromptResourceTest : IntegrationTestBase() {
  @Autowired
  private lateinit var promptRepository: PromptRepository

  @Autowired
  private lateinit var promptVersionRepository: PromptVersionRepository

  @Autowired
  private lateinit var mapper: ObjectMapper
  private val promptKey = UUID.randomUUID().toString()
  private val createdBy = UUID.randomUUID()

  @BeforeEach
  fun setup() {
    webTestClient = webTestClient
      .mutate()
      .responseTimeout(Duration.ofMillis(30000))
      .build()
    runBlocking {
      promptVersionRepository.deleteAll()
      promptRepository.deleteAll()
    }
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
            mapper.readTree(DataGenerator.jsonRequestSchema),
            mapper.readTree(DataGenerator.jsonResponseSchema),
          ),
        ),
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
    val key = UUID.randomUUID().toString()
    val response = webTestClient.post().uri("/v1/prompts")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        PromptRequest(
          key,
          "AI SERVICE",
          createdBy,
          PromptVersionRequest(
            "TEST-MODEL-5",
            "Get json output of firstname and lastname.",
            mapper.readTree(DataGenerator.jsonRequestSchema),
            mapper.readTree(DataGenerator.jsonResponseSchema),
          ),
        ),
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
    assertEquals(key, response.promptKey)
    assertEquals(1, response.promptVersion.version)
    assertEquals(createdBy, response.createdBy)
    assertEquals(mapper.readTree(DataGenerator.jsonRequestSchema.trimIndent()), response.promptVersion.requestContract)
    assertEquals(mapper.readTree(DataGenerator.jsonResponseSchema.trimIndent()), response.promptVersion.responseContract)
  }

  @Test
  fun updatePrompt() {
    val response = webTestClient.put().uri("/v1/prompts/$promptKey")
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
            mapper.readTree(DataGenerator.jsonRequestSchema),
            mapper.readTree(DataGenerator.jsonResponseSchema),
          ),
        ),
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<PromptResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as PromptResponse

    assertNotNull(response)
    assertEquals(2, response.promptVersion.version)
  }

  @Test
  fun `get prompts`() {
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
    assertEquals(1, res.size)
  }

  @Test
  fun `get Prompt by key`() {
    val response = webTestClient.get().uri("/v1/prompts/$promptKey")
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
    assertNotNull(response)
    assertEquals(promptKey, response.promptKey)
    assertEquals(1, response.promptVersion.version)
    assertEquals(createdBy, response.createdBy)
  }

  @Test
  fun `get prompt by random key`() {
    val response = webTestClient.get().uri("/v1/prompts/${UUID.randomUUID()}")
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
    val response = webTestClient.get().uri("/v1/prompts/$promptKey/versions/1")
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
    assertNotNull(response)
    assertEquals(promptKey, response.promptKey)
    assertEquals(1, response.promptVersion.version)
    assertEquals(createdBy, response.createdBy)
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
