package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import com.fasterxml.uuid.Generators
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Status
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestHistoryRepositoryTest : IntegrationTestBase() {
  @Autowired lateinit var requestHistoryRepository: RequestHistoryRepository
  private lateinit var history: RequestHistory

  @BeforeAll
  fun setUp() {
    runBlocking {
      requestHistoryRepository.deleteAll()
      history = RequestHistory(
        Generators.timeBasedEpochGenerator().generate(),
        true,
        UUID.randomUUID(),
        UUID.randomUUID(),
        LocalDateTime.now(ZoneOffset.UTC),
        LocalDateTime.now(ZoneOffset.UTC).plusSeconds(10L),
        LocalDateTime.now(ZoneOffset.UTC).plusSeconds(40L),
        Status.SUCCEEDED,
        null,
        null,
      )
    }
  }

  @AfterAll
  fun tearDown() {
    runBlocking {
      requestHistoryRepository.deleteAll()
    }
  }

  @Test
  fun `save history request`() {
    runBlocking {
      val entity = requestHistoryRepository.save(history)
      Assertions.assertNotNull(entity)
      assertRequestHistory(history, entity)
    }
  }

  @Test
  fun `get request by id`() {
    runBlocking {
      requestHistoryRepository.findById(history.id!!)?.let { entity ->
        assertRequestHistory(history, entity)
      }
    }
  }

  private fun assertRequestHistory(expected: RequestHistory, actual: RequestHistory) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.correlationId, actual.correlationId)
    assertEquals(expected.synchronousRequest, actual.synchronousRequest)
    assertEquals(expected.status, actual.status)
    assertEquals(expected.promptVersionId, actual.promptVersionId)
    assertEquals(expected.errorMessage, actual.errorMessage)
  }
}
