package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

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
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator.Companion.jsonRequestSchema
import uk.gov.justice.digital.hmpps.justicedataagentworker.utility.DataGenerator.Companion.jsonResponseSchema

@SpringBootTest(classes = [PromptRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.justicedataagentworker.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.justicedataagentworker.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PromptRepositoryTest {

  @Autowired
  private lateinit var promptRepository: PromptRepository

  private lateinit var prompt: Prompt
  private lateinit var promptVersion: PromptVersion

  @BeforeAll
  fun setUp() {
    promptRepository.deleteAll()
    prompt = DataGenerator.buildPrompt(mutableSetOf())
    promptVersion = DataGenerator.buildPromptVersion(prompt, jsonRequestSchema, jsonResponseSchema)
  }

  @AfterAll
  fun tearDown() {
    promptRepository.deleteAll()
  }

  @Test
  fun `create prompt`() {
    val entity = promptRepository.save(prompt)

    Assertions.assertNotNull(entity)
    assertPrompt(prompt, entity)
    assertEquals(0, entity.promptVersions.size)
    val promptVersion = DataGenerator.buildPromptVersion(prompt, jsonRequestSchema, jsonResponseSchema)
    entity.promptVersions.add(promptVersion)
    val updatedEntity = promptRepository.save(entity)
    assertPrompt(prompt, updatedEntity)
  }

  fun `get prompt by id`() {
    val entity = promptRepository.findById(prompt.id).get()
    assertEquals(prompt.id, entity.id)
    assertPrompt(prompt, entity)
  }

  private fun assertPrompt(expected: Prompt, actual: Prompt) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.promptKey, actual.promptKey)
    assertEquals(expected.description, actual.description)
    assertEquals(expected.createdBy, actual.createdBy)
  }
}
