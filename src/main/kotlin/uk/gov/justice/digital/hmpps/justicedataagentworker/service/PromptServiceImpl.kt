package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.fasterxml.uuid.Generators
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.PromptRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class PromptServiceImpl(private val promptRepository: PromptRepository) : PromptService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun savePrompt(promptRequest: PromptRequest): PromptResponse {
    logger.info("saving prompt created by ${promptRequest.createdBy}")
    convertPromptRequestToEntity(promptRequest)
    var response: PromptResponse? = null
    promptRepository.save(convertPromptRequestToEntity(promptRequest))?.let { entity ->
      response = convertPromptToPromptResponse(entity)
    }
    logger.info("returning prompt created by ${promptRequest.createdBy}")
    return response!!
  }

  override suspend fun getPromptById(id: UUID): PromptResponse {
    var prompt: Prompt? = null
    promptRepository.findById(id)?.let { promptEntity ->
      prompt = promptEntity
    }
    if (prompt == null) {
      throw NotFoundException("Prompt with id $id not found")
    }
    return convertPromptToPromptResponse(prompt)
  }

  override suspend fun deletePromptById(id: UUID) {
    TODO("Not yet implemented")
  }

  private fun convertPromptRequestToEntity(promptRequest: PromptRequest): Prompt = Prompt(
    Generators.timeBasedEpochGenerator().generate(),
    promptRequest.promptKey,
    promptRequest.description,
    false,
    promptRequest.createdBy,
    LocalDateTime.now(ZoneOffset.UTC),
  )

  private fun convertPromptToPromptResponse(prompt: Prompt): PromptResponse = PromptResponse(
    prompt.id,
    prompt.promptKey,
    prompt.description,
    prompt.isDeleted,
    prompt.createdBy,
    prompt.createdDate,
  )
}
