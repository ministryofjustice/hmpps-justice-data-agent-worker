package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import java.util.UUID

interface PromptService {
  suspend fun savePrompt(promptRequest: PromptRequest): PromptResponse

  suspend fun getPromptById(id: UUID): PromptResponse

  suspend fun deletePromptById(id: UUID)
}
