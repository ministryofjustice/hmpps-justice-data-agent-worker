package uk.gov.justice.digital.hmpps.justicedataagentworker.integration

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.config.LocalStackContainer
import uk.gov.justice.digital.hmpps.justicedataagentworker.config.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.justicedataagentworker.config.PostgresContainer
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.LiteLlmApiExtension
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import kotlin.text.toInt

@ExtendWith(HmppsAuthApiExtension::class, LiteLlmApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class IntegrationTestBase internal constructor() {

  @Autowired private lateinit var flyway: Flyway

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  internal val jdaRequestQueues by lazy { hmppsQueueService.findByQueueId("jdarequestqueues") as HmppsQueue }
  internal val jdaRequestQueuesAwsSqsClient by lazy { jdaRequestQueues.sqsClient }
  internal val jdaRequestQueueUrl by lazy { jdaRequestQueues.queueUrl }

  internal val jdaResponseQueues by lazy { hmppsQueueService.findByQueueId("jdarequestqueues") as HmppsQueue }
  internal val jdaResponseQueuesAwsSqsClient by lazy { jdaResponseQueues.sqsClient }
  internal val jdaResponseQueueUrl by lazy { jdaResponseQueues.queueUrl }

  companion object {

    @JvmStatic
    private val container = PostgresContainer.postgres

    @JvmStatic
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun localstackProperties(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }

    @BeforeAll
    @JvmStatic
    fun startContainer() {
      // postgresContainer.start()
      // flyway
      localStackContainer?.start()
    }

    @AfterAll
    @JvmStatic
    fun stopContainer() {
      // container.stop()
    }

    @DynamicPropertySource
    @JvmStatic
    fun setProperties(registry: DynamicPropertyRegistry) {
      container?.run {
        registry.add("spring.r2dbc.url", { container.getJdbcUrl().replace("jdbc", "r2dbc") })
        registry.add("spring.r2dbc.username", container::getUsername)
        registry.add("spring.r2dbc.password", container::getPassword)
        registry.add("spring.flyway.url", container::getJdbcUrl)
        registry.add("spring.flyway.user", container::getUsername)
        registry.add("spring.flyway.password", container::getPassword)
      }
    }
  }

  internal fun getNumberOfMessagesCurrentlyOnQueue(awsSqsClient: SqsAsyncClient, queueUrl: String): Int? {
    val queueAttributes = awsSqsClient.getQueueAttributes(
      GetQueueAttributesRequest.builder().queueUrl(queueUrl).attributeNamesWithStrings("ApproximateNumberOfMessages").build(),
    ).get()
    return queueAttributes.attributesAsStrings()["ApproximateNumberOfMessages"]?.toInt()
  }

  @BeforeEach
  fun `Wait for empty queue`() {
    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue(jdaRequestQueuesAwsSqsClient, jdaRequestQueueUrl) } matches { it == 0 }

    await untilCallTo { getNumberOfMessagesCurrentlyOnQueue(jdaResponseQueuesAwsSqsClient, jdaResponseQueueUrl) } matches { it == 0 }
  }

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }
}
