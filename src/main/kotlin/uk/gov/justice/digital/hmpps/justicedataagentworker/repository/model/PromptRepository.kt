package uk.gov.justice.digital.hmpps.justicedataagentworker.repository.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import java.util.UUID

@Repository
interface PromptRepository : JpaRepository<Prompt, UUID>