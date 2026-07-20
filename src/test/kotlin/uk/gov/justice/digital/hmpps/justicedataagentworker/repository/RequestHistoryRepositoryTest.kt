package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import com.fasterxml.uuid.Generators
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Status
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [RequestHistoryRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.justicedataagentworker.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.justicedataagentworker.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequestHistoryRepositoryTest {
  @Autowired lateinit var requestHistoryRepository: RequestHistoryRepository
  private lateinit var history: RequestHistory

  @BeforeAll
  fun setUp() {
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

  @AfterAll
  fun tearDown() {
    requestHistoryRepository.deleteAll()
  }

  @Test
  fun `save history request`() {
    val entity = requestHistoryRepository.save(history)
    Assertions.assertNotNull(entity)
    assertEquals(history, entity)
  }

  @Test
  fun `get request by id`() {
    val entity = requestHistoryRepository.findById(history.id).get()
    assertEquals(history, entity)
  }

  private fun assertRequestHistory(expected: RequestHistory, actual: RequestHistory) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.correlationId, actual.correlationId)
    assertEquals(expected.synchronousRequest, actual.synchronousRequest)
    assertEquals(expected.status, actual.status)
    assertEquals(expected.completedAt, actual.completedAt)
    assertEquals(expected.promptVersionId, actual.promptVersionId)
    assertEquals(expected.errorMessage, actual.errorMessage)
    assertEquals(expected.errorAt, actual.errorAt)
    assertEquals(expected.queuedAt, actual.queuedAt)
    assertEquals(expected.receivedAt, actual.receivedAt)
  }
}
