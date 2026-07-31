package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import java.util.UUID

@Service
class PromptVersionServiceImpl(
  private val promptRepository: PromptRepository,
  private val promptVersionRepository: PromptVersionRepository,
) : PromptVersionService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun savePromptVersion(promptVersionRequest: PromptVersionRequest): PromptVersionResponse {
    TODO("Not yet implemented")
    /*val prompt = promptRepository.findById(promptVersionRequest.promptId)
    if (prompt == null) {
      throw NotFoundException("Prompt with ${promptVersionRequest.promptId} not found")
    }
    var promptVersion = convertPromptVersionRequestToEntity(promptVersionRequest)
    logger.info("Saving prompt version for client: ${promptVersion.createdBy}, version ${promptVersion.version}")
    promptVersion = promptVersionRepository.save(promptVersion)
    return convertEntityToPromptVersionResponse(promptVersion)*/
  }

  override suspend fun getPromptVersionById(id: UUID): PromptVersionResponse {
    TODO("Not yet implemented")
    /*var promptVersion: PromptVersion? = null
    promptVersionRepository.findById(id)?.let { entity -> promptVersion = entity }
    if (promptVersion == null) {
      throw NotFoundException("Prompt Version with id $id not found")
    }
    return convertEntityToPromptVersionResponse(promptVersion)*/
  }

  override suspend fun getPromptVersionByKeyAndVersion(key: String, version: Int): PromptVersionResponse {
    TODO("Not yet implemented")
    /*promptRepository.save(buildPrompt(mutableSetOf()))
    val prompt = promptRepository.findPromptByPromptKey(key)
    var promptVersion = promptVersionRepository.findPromptVersionByPromptIdAndVersion(prompt?.id!!, version)
    if (promptVersion == null) {
      throw NotFoundException("Prompt Version with id $key and $version not found")
    }
    return convertEntityToPromptVersionResponse(promptVersion)*/
  }

  override suspend fun updatePromptVersionById(
    id: UUID,
    promptVersion: PromptVersion,
  ): PromptVersionResponse {
    TODO("Not yet implemented")
  }

  override suspend fun deletePromptVersionById(id: UUID) {
    TODO("Not yet implemented")
  }

  /*private fun convertPromptVersionRequestToEntity(promptVersionRequest: PromptVersionRequest): PromptVersion {
    val responseFormat =
      if (promptVersionRequest.responseContract != null) Json.of(promptVersionRequest.responseContract) else null
    return PromptVersion(
      Generators.timeBasedEpochGenerator().generate(),
      promptVersionRequest.version,
      promptVersionRequest.promptId,
      promptVersionRequest.llmModel,
      promptVersionRequest.promptTemplate,
      Json.of(promptVersionRequest.requestContract),
      responseFormat,
      promptVersionRequest.createdBy,
      LocalDateTime.now(ZoneOffset.UTC),
    )
  }*/

  /*private fun convertEntityToPromptVersionResponse(promptVersion: PromptVersion): PromptVersionResponse = PromptVersionResponse(
    promptVersion.id!!,
    promptVersion.version,
    promptVersion.promptId,
    promptVersion.llmModel,
    promptVersion.promptTemplate,
    promptVersion.requestContract.asString(),
    promptVersion.responseContract?.asString(),
    promptVersion.createdBy,
    promptVersion.createdDate,
  )*/

  /*fun buildPrompt(promptVersion: MutableSet<PromptVersion>): Prompt = Prompt(
    Generators.timeBasedEpochGenerator().generate(),
    UUID.randomUUID().toString(),
    // promptVersion,
    "Inline instruction  FOR LLM",
    false,
    UUID.randomUUID(),
    LocalDateTime.now(ZoneOffset.UTC),
  )*/
}
