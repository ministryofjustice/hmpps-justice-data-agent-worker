package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.fasterxml.uuid.Generators
import io.r2dbc.postgresql.codec.Json
import org.hibernate.reactive.common.Identifier.id
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptVersionRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    // find prompt with id exist
    var prompt: Prompt? = null
    var promptVersionResponse: PromptVersionResponse? = null
    promptRepository.findById(promptVersionRequest.promptId)?.let { entity ->
      prompt = entity
    }
    if (prompt == null) {
      throw NotFoundException("Prompt with ${promptVersionRequest.promptId} not found")
    }

    val promptVersion = convertPromptVersionRequestToEntity(promptVersionRequest)
    promptVersionRepository.save(promptVersion)?.let { entity ->
      promptVersionResponse =  convertEntityToPromptVersionResponse(entity)
    }
    return promptVersionResponse!!
  }

  override suspend fun getPromptVersionById(id: UUID): PromptVersionResponse {
    var promptVersion: PromptVersion? = null
    promptVersionRepository.findById(id)?.let { entity -> promptVersion = entity }
    if (promptVersion == null) {
      throw NotFoundException("Prompt Version with id $id not found")
    }
    return convertEntityToPromptVersionResponse(promptVersion!!)
  }

  override suspend fun getPromptVersionByKeyAndVersion(key: String, version: Int): PromptVersionResponse {
    var prompt: Prompt? = null
    promptRepository.findPromptByPromptKey(key).let { entity -> prompt = entity }

    var promptVersion: PromptVersion? = null
    promptVersionRepository.findPromptVersionByPromptIdAndVersion(prompt?.id!!, version).let { entity -> promptVersion = entity }
    if (promptVersion == null) {
      throw NotFoundException("Prompt Version with id $key and $version not found")
    }
    return convertEntityToPromptVersionResponse(promptVersion!!)
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

  private fun convertPromptVersionRequestToEntity(promptVersionRequest: PromptVersionRequest) : PromptVersion {
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
  }

  private fun convertEntityToPromptVersionResponse(promptVersion: PromptVersion): PromptVersionResponse {
    return PromptVersionResponse(
      promptVersion.id,
      promptVersion.version,
      promptVersion.promptId,
      promptVersion.llmModel,
      promptVersion.promptTemplate,
      promptVersion.requestContract.toString(),
      promptVersion.requestContract.toString(),
      promptVersion.createdBy,
      promptVersion.createdDate,
    )
  }
}
