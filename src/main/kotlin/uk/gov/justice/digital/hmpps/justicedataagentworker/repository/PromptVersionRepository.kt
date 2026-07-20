package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.util.UUID

@Repository
interface PromptVersionRepository : JpaRepository<PromptVersion, UUID>
