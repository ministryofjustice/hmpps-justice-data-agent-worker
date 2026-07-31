package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

// import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import java.util.*

@Repository
interface PromptRepository : CoroutineCrudRepository<Prompt, UUID> {
  suspend fun findPromptByPromptKeyAndIsDeleted(promptKey: String, isDeleted: Boolean): Prompt?
}
