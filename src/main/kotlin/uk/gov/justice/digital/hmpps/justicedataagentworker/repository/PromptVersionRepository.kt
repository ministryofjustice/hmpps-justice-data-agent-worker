package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.util.UUID

@Repository
interface PromptVersionRepository :
  CoroutineCrudRepository<PromptVersion, UUID>,
  CoroutineSortingRepository<PromptVersion, UUID> {
  suspend fun findPromptVersionByPromptIdAndVersion(promptId: UUID, version: Int): PromptVersion?

  suspend fun findFirstByPromptIdOrderByVersionDesc(promptId: UUID): PromptVersion?

  suspend fun findPromptVersionsByPromptIdOrderByVersionAsc(promptId: UUID): List<PromptVersion>

}
