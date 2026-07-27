package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class PromptVersionRepositoryTest : IntegrationTestBase() {

  @Autowired private lateinit var promptRepository: PromptRepository

  @Autowired private lateinit var promptVersionRepository: PromptVersionRepository

  private lateinit var prompt: Prompt
  private lateinit var promptVersion: PromptVersion

  @BeforeAll
  fun setUp() {
    runBlocking {
      prompt = DataGenerator.buildPrompt(mutableSetOf())
      promptRepository.save(prompt)
      promptVersion = DataGenerator.buildPromptVersion(prompt.id!!, jsonRequestSchema, jsonResponseSchema)
      promptVersionRepository.save(promptVersion)
    }
  }

  @AfterAll
  fun tearDown() {
    runBlocking {
      promptVersionRepository.deleteAll()
      promptRepository.deleteAll()
    }
  }

  @Test
  fun `create prompt version`() {
    runBlocking {
      val promptVersion = DataGenerator.buildPromptVersion(prompt.id!!, jsonRequestSchema, jsonResponseSchema)
      val entity = promptVersionRepository.save(promptVersion)
      assertNotNull(entity)
      assertPromptVersion(promptVersion, entity)
    }
  }

  @Test
  fun `get  prompt version by id`() {
    runBlocking {
      promptVersionRepository.findById(promptVersion.id!!)?.let { entity ->
        assertNotNull(entity)
        assertPromptVersion(promptVersion, entity)
      }
    }
  }

  private fun assertPromptVersion(expected: PromptVersion, actual: PromptVersion) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.promptTemplate, actual.promptTemplate)
    assertEquals(expected.promptId, actual.promptId)
    assertEquals(expected.llmModel, actual.llmModel)
    assertEquals(expected.version, actual.version)
    // assertEquals(expected.requestContract, actual.requestContract)
    // assertEquals(expected.responseContract, actual.responseContract)
    assertEquals(expected.createdBy, actual.createdBy)
  }
}
