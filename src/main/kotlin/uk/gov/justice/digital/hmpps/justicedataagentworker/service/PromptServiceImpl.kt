package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.fasterxml.uuid.Generators
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.stream.consumeAsFlow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptsResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.RecordAlreadyExist
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class PromptServiceImpl(
  private val promptRepository: PromptRepository,
  private val promptVersionRepository: PromptVersionRepository,
  private val mapper: ObjectMapper,
) : PromptService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun savePrompt(promptRequest: PromptRequest): PromptResponse {
    logger.info("saving prompt created by ${promptRequest.createdBy}")
    val prompt = promptRepository.findPromptByPromptKeyAndIsDeleted(promptRequest.promptKey, false)
    if (prompt != null) {
      throw RecordAlreadyExist("Prompt with key ${promptRequest.promptKey} already exists.")
    }
    var promptEntity = convertPromptRequestToEntity(promptRequest)
    promptEntity = promptRepository.save(promptEntity)
    var promptVersion = promptVersionRepository.findFirstByPromptIdOrderByVersionDesc(promptEntity.id)
    var version = 1
    if (promptVersion != null) {
      version = promptVersion.version + 1
    }
    promptVersion = convertPromptVersionRequestToEntity(promptEntity.id, promptEntity.createdBy, version, promptRequest.promptVersion)
    val promptVersionEntity = promptVersionRepository.save(promptVersion)
    logger.info("returning prompt created by ${promptRequest.createdBy}")
    return convertPromptToPromptResponse(promptEntity, promptVersionEntity)
  }

  override suspend fun updatePrompt(key: String, promptRequest: PromptRequest): PromptResponse {
    logger.info("Getting prompt from db by key $key")
    val prompt = promptRepository.findPromptByPromptKeyAndIsDeleted(promptRequest.promptKey, false)
    if (prompt == null) {
      throw NotFoundException("Prompt with key ${promptRequest.promptKey} not Found.")
    }
    var promptVersion = promptVersionRepository.findFirstByPromptIdOrderByVersionDesc(prompt.id)
    if (promptVersion == null) {
      throw NotFoundException("Prompt with key ${promptRequest.promptKey} do not have any prompt version")
    }
    val version = promptVersion.version + 1
    promptVersion = convertPromptVersionRequestToEntity(prompt.id, prompt.createdBy, version, promptRequest.promptVersion)
    val promptVersionEntity = promptVersionRepository.save(promptVersion)
    logger.info("returning updated prompt for key ${promptRequest.promptKey}")
    return convertPromptToPromptResponse(prompt, promptVersionEntity)
  }

  override suspend fun getPrompts(): List<PromptsResponse> {
    val prompts = mutableListOf<Prompt>()
    promptRepository.findAll().collect { prompt -> prompts.add(prompt) }
    prompts.stream().consumeAsFlow().collect { prompt ->
      promptVersionRepository.findPromptVersionsByPromptIdOrderByVersionAsc(prompt.id)
    }
    val promptsResponse = mutableListOf<PromptsResponse>()
    prompts.asFlow().collect { prompt ->
      val prt = promptVersionRepository.findPromptVersionsByPromptIdOrderByVersionAsc(prompt.id)
      promptsResponse.add(
        PromptsResponse(
          prompt.id,
          prompt.promptKey,
          prompt.description,
          prompt.isDeleted,
          prompt.createdBy,
          prompt.createdDate,
          convertPromptsToPromptsResponse(prt),
        ),
      )
    }
    return promptsResponse
  }

  override suspend fun getPromptsByKeyAndVersion(key: String, version: Int): PromptResponse {
    val prompt = promptRepository.findPromptByPromptKeyAndIsDeleted(key, false)
    if (prompt == null) {
      throw NotFoundException("Prompt with key $key not found.")
    }
    val promptVersion = promptVersionRepository.findPromptVersionByPromptIdAndVersion(prompt.id, version)
    if (promptVersion == null) {
      throw NotFoundException("Prompt with version $version not found.")
    }
    return convertPromptToPromptResponse(prompt = prompt, promptVersion = promptVersion)
  }

  override suspend fun getPromptByKey(key: String): PromptResponse {
    val prompt = promptRepository.findPromptByPromptKeyAndIsDeleted(key, false)
    if (prompt == null) {
      throw NotFoundException("Prompt with key $key not found.")
    }
    val version = promptVersionRepository.findFirstByPromptIdOrderByVersionDesc(prompt?.id!!)
    return convertPromptToPromptResponse(prompt = prompt, promptVersion = version!!)
  }

  override suspend fun deletePromptByKey(key: String) {
    val prompt = promptRepository.findPromptByPromptKeyAndIsDeleted(key, false)
    if (prompt == null) {
      throw NotFoundException("Prompt with key $key not found.")
    }
    prompt.isDeleted = true
    prompt.new = false
    promptRepository.save(prompt)
  }

  private fun convertPromptRequestToEntity(promptRequest: PromptRequest): Prompt = Prompt(
    Generators.timeBasedEpochGenerator().generate(),
    promptRequest.promptKey,
    promptRequest.description,
    false,
    promptRequest.createdBy,
    LocalDateTime.now(ZoneOffset.UTC),
  )

  fun convertPromptVersionRequestToEntity(promptId: UUID, createdBy: UUID, version: Int, promptVersionRequest: PromptVersionRequest): PromptVersion = PromptVersion(
    Generators.timeBasedEpochGenerator().generate(),
    version,
    promptId,
    promptVersionRequest.llmModel,
    promptVersionRequest.promptTemplate,
    Json.of(mapper.writeValueAsString(promptVersionRequest.requestContract)),
    promptVersionRequest.responseContract?.let { Json.of(mapper.writeValueAsString(it)) },
    createdBy,
    LocalDateTime.now(ZoneOffset.UTC),
  )

  private fun convertPromptToPromptResponse(prompt: Prompt, promptVersion: PromptVersion): PromptResponse = PromptResponse(
    prompt.id,
    prompt.promptKey,
    prompt.description,
    prompt.isDeleted,
    prompt.createdBy,
    prompt.createdDate,
    PromptVersionResponse(
      promptVersion.id,
      promptVersion.version,
      promptVersion.llmModel,
      promptVersion.promptTemplate,
      mapper.readTree(promptVersion.requestContract.asString()),
      promptVersion.responseContract?.let { mapper.readTree(it.asString()) },
      promptVersion.createdBy,
      promptVersion.createdDate,
    ),
  )

  private fun convertPromptsToPromptsResponse(promptVersions: List<PromptVersion>): List<PromptVersionResponse> {
    val promptVersionsResponse = mutableListOf<PromptVersionResponse>()
    promptVersions.forEach { promptVersion ->
      promptVersionsResponse.add(
        PromptVersionResponse(
          promptVersion.id,
          promptVersion.version,
          promptVersion.llmModel,
          promptVersion.promptTemplate,
          mapper.readTree(promptVersion.requestContract.asString()),
          promptVersion.responseContract?.let { mapper.readTree(it.asString()) },
          promptVersion.createdBy,
          promptVersion.createdDate,
        ),
      )
    }
    return promptVersionsResponse
  }
}
