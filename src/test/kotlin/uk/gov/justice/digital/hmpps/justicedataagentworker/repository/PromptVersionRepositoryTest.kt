package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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

@SpringBootTest(classes = [PromptRepository::class, PromptVersionRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.justicedataagentworker.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.justicedataagentworker.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PromptVersionRepositoryTest {

  @Autowired private lateinit var promptRepository: PromptRepository

  @Autowired private lateinit var promptVersionRepository: PromptVersionRepository

  private lateinit var prompt: Prompt
  private lateinit var promptVersion: PromptVersion

  @BeforeAll
  fun setUp() {
    prompt = DataGenerator.buildPrompt(mutableSetOf())
    promptRepository.save(prompt)
    promptVersion = DataGenerator.buildPromptVersion(prompt, jsonRequestSchema, jsonResponseSchema)
    promptVersionRepository.save(promptVersion)
  }

  @AfterAll
  fun tearDown() {
    promptRepository.deleteAll()
    promptVersionRepository.deleteAll()
  }

  @Test
  fun `create prompt version`() {
    val promptVersion = DataGenerator.buildPromptVersion(prompt, jsonRequestSchema, jsonResponseSchema)
    val entity = promptVersionRepository.save(promptVersion)
    assertNotNull(entity)
    assertPromptVersion(promptVersion, entity)
  }

  @Test
  fun `get  prompt version by id`() {
    val entity = promptVersionRepository.findById(promptVersion.id).get()
    assertNotNull(entity)
    assertPromptVersion(promptVersion, entity)
  }

  private fun assertPromptVersion(expected: PromptVersion, actual: PromptVersion) {
    assertEquals(expected.id, actual.id)
    assertEquals(expected.promptTemplate, actual.promptTemplate)
    assertEquals(expected.prompt, actual.prompt)
    assertEquals(expected.version, actual.version)
    assertEquals(expected.requestContract, actual.requestContract)
    assertEquals(expected.responseContract, actual.responseContract)
    assertEquals(expected.createdBy, actual.createdBy)
  }
}
