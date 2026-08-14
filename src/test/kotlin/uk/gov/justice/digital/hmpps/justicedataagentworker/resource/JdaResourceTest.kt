package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import java.time.Duration
import java.util.UUID

class JdaResourceTest : IntegrationTestBase() {

  @Autowired private lateinit var promptRepository: PromptRepository

  @Autowired private lateinit var promptVersionRepository: PromptVersionRepository

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
}
