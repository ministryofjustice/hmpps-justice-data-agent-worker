package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.util.UUID

interface PromptVersionService {

  suspend fun savePromptVersion(promptVersionRequest: PromptVersionRequest): PromptVersionResponse

  suspend fun getPromptVersionById(id: UUID): PromptVersionResponse

  suspend fun getPromptVersionByKeyAndVersion(key: String, version: Int): PromptVersionResponse

  suspend fun updatePromptVersionById(id: UUID, promptVersion: PromptVersion): PromptVersionResponse

  suspend fun deletePromptVersionById(id: UUID)
}
