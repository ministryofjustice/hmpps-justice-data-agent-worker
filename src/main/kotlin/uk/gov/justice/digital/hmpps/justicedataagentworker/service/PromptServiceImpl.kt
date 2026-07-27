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
    val prompt = convertPromptRequestToEntity(promptRequest)
    val entity = promptRepository.save(prompt)
    logger.info("returning prompt created by ${promptRequest.createdBy}")
    return convertPromptToPromptResponse(entity)
  }

  override suspend fun getPromptById(id: UUID): PromptResponse {
    logger.info("Getting prompt by id: $id")
    val prompt = promptRepository.findById(id)
    if (prompt == null) {
      throw NotFoundException("Prompt with id $id not found")
    }
    logger.info("returning prompt by id: $id")
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
