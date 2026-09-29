package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.JdaValidationException
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import java.util.UUID

class JdaBatchProcesserTest(@Autowired private val jdaBatchProcesser: JdaBatchProcesser) : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `test batch process`() {
    val jdaRequest = DataGenerator.buildJdaRequest(UUID.randomUUID(), "test key", 1)
    val value = ObjectMapper().writeValueAsString(jdaRequest)
    val req = ObjectMapper().readValue(value, JdaRequest::class.java)
    var jdaRequests: List<JdaRequest> = mutableListOf()
    runBlocking {
      jdaRequests = jdaBatchProcesser.processJdaRequest(req, 1)
    }
    assertEquals(5, jdaRequests.size)
    runBlocking {
      jdaRequests = jdaBatchProcesser.processJdaRequest(req, 0)
    }
    assertEquals(1, jdaRequests.size)
  }

  @Test
  fun `test batch process for request data not array`() {
    val jdaRequest = DataGenerator.buildJdaRequest(UUID.randomUUID(), "test key", 1)
    jdaRequest.requestData = """
      {
      "case_not_id": "${UUID.randomUUID()}",
      "case_note_text: "test data text"
      }
    """.trimIndent()
    val value = ObjectMapper().writeValueAsString(jdaRequest)
    val req = ObjectMapper().readValue(value, JdaRequest::class.java)
    assertThrows(JdaValidationException::class.java) {
      runBlocking {
        jdaBatchProcesser.processJdaRequest(req, 1)
      }
    }
  }

  @Test
  fun `test batch process for batch size null`() {
    val jdaRequest = DataGenerator.buildJdaRequest(UUID.randomUUID(), "test key", 1)
    val value = ObjectMapper().writeValueAsString(jdaRequest)
    val req = ObjectMapper().readValue(value, JdaRequest::class.java)
    var jdaRequests: List<JdaRequest> = mutableListOf()
    runBlocking {
      jdaRequests = jdaBatchProcesser.processJdaRequest(req, null)
    }
    assertEquals(1, jdaRequests.size)
  }
}
