package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import java.util.UUID

interface RequestHistoryService {

  suspend fun saveRequestHistory(request: RequestHistory)

  suspend fun getRequestHistoryById(id: UUID): RequestHistory

  suspend fun deleteRequestHistoryById(id: UUID)
}
