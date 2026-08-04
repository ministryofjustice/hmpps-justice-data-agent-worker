package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import java.util.*

@Repository
interface PromptRepository : CoroutineCrudRepository<Prompt, UUID> {
  // isDeleted = false as record exist and isDeleted = true as record do not exist
  suspend fun findPromptByPromptKeyAndIsDeleted(promptKey: String, isDeleted: Boolean): Prompt?
}
