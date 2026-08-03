package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import java.time.Duration
import java.util.UUID

class JdaResourceTest : IntegrationTestBase() {

 private lateinit var promptKey: String
  @BeforeEach
  internal fun setUp(

  ) {
    promptKey = UUID.randomUUID().toString()
    webTestClient = webTestClient
      .mutate()
      .responseTimeout(Duration.ofMillis(30000))
      .build()
  }

  /*@Test
  fun analyze() {
    webTestClient.post()
    .uri("/v1/sendrequest")
      .headers(setAuthorisation(roles = listOf("ROLE_JUSTICE_DATA_AGENT_PROMPTS")))
      .header("Content-Type", "application/json")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(
        JdaRequest(
          UUID.randomUUID(),
          Prompt(promptKey,
            1
            ),
           ""
        )
      )
  }*/

}