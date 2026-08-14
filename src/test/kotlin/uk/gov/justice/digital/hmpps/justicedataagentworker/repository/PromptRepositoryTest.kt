package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

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
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator.Companion.jsonRequestSchema
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator.Companion.jsonResponseSchema

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PromptRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var promptRepository: PromptRepository

  @Autowired
  private lateinit var promptVersionRepository: PromptVersionRepository

  private lateinit var prompt: Prompt
  private lateinit var promptVersion: PromptVersion

  @BeforeAll
  fun setUp() {
    runBlocking {
      prompt = DataGenerator.buildPrompt(mutableSetOf())
      promptVersion = DataGenerator.buildPromptVersion(prompt.id!!, jsonRequestSchema, jsonResponseSchema)
      promptVersionRepository.deleteAll()
      promptRepository.deleteAll()
    }
  }

  @AfterAll
  fun tearDown() {
    runBlocking {
      promptRepository.deleteAll()
    }
  }

  @Test
  fun `create prompt`() {
    runBlocking {
      val entity = promptRepository.save(prompt)

      Assertions.assertNotNull(entity)
      assertPrompt(prompt, entity)
      entity.new = false
      val updatedEntity = promptRepository.save(entity)
      assertPrompt(prompt, updatedEntity)
    }
  }

  @Test
  fun `get prompt by id`() {
    runBlocking {
      promptRepository.findById(prompt.id!!)?.let { entity ->
        assertEquals(prompt.id, entity.id)
        assertPrompt(prompt, entity)
      }
    }
  }

  private fun assertPrompt(expected: Prompt, actual: Prompt) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.promptKey, actual.promptKey)
    assertEquals(expected.description, actual.description)
    assertEquals(expected.createdBy, actual.createdBy)
  }
}
