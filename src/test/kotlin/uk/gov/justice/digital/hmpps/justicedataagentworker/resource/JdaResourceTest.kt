package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import io.awspring.cloud.sqs.operations.SqsTemplate
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.event.JdaMessagePublisherImpl.Companion.logger
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import java.time.Duration
import java.util.UUID

class JdaResourceTest(
  @param:Value("\${hmpps.sqs.queues.jdarequestqueues.queuename}") val jdaRequestQueueName: String,
  @param:Value("\${hmpps.sqs.queues.jdaresponsequeues.queuename}") val jdaResponseQueueName: String,
) : IntegrationTestBase() {

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
      .responseTimeout(Duration.ofMillis(300000))
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
            // null//mapper.readTree(DataGenerator.jsonResponseSchema),
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

  @Test
  fun `submit synchronous request`() {
    webTestClient.post().uri("/v1/submitrequest")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_REQUESTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        DataGenerator.buildJdaRequest(UUID.randomUUID(), promptKey, 1),
      )
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
      .expectBody(object : ParameterizedTypeReference<JdaResponse>() {})
      .consumeWith(System.out::println)
      .returnResult()
      .responseBody as JdaResponse
  }

  @Test
  fun `submit async queue request to request queue and publish llm response to response queue`() {
    val correlationId = UUID.randomUUID()
    val promptKey = promptKey
    val version = 1
    val jdaRequest = DataGenerator.buildJdaRequest(correlationId, promptKey, version)
    val sqsTemplate =
      SqsTemplate
        .newTemplate(requestQueueAwsSqsClient)
    logger.info("Sending jda response message to response queue: $jdaRequestQueueName")
    sqsTemplate.send { to -> to.queue(jdaRequestQueueName).payload(jdaRequest) }

    Thread.sleep(Duration.ofSeconds(15))
    val messages = responseQueueAwsSqsClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .maxNumberOfMessages(1)
        .queueUrl(responseQueueUrl)
        .build(),
    )?.join()
    val jdaResponse = mapper.readValue(messages?.messages()[0]?.body(), JdaResponse::class.java)
    assertEquals(1, messages?.messages()?.size)
    assertEquals(correlationId, jdaResponse.correlationId)
    assertEquals(promptKey, jdaResponse.prompt.key)
    assertEquals(version, jdaResponse.prompt.version)
  }
}
