package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

// import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.util.UUID

@Repository
interface PromptRepository :
  CoroutineCrudRepository<Prompt, UUID>,
  CoroutineSortingRepository<Prompt, UUID> {
  suspend fun findPromptByPromptKey(key: String): Prompt?
}
