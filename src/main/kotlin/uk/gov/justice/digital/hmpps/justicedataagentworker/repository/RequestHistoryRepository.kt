package uk.gov.justice.digital.hmpps.justicedataagentworker.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import java.util.UUID

@Repository
interface RequestHistoryRepository : JpaRepository<RequestHistory, UUID>