package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.NotFoundException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.repository.RequestHistoryRepository
import java.util.UUID

@Service
class RequestHistoryServiceImpl(private val requestHistoryRepository: RequestHistoryRepository) : RequestHistoryService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun saveRequestHistory(request: RequestHistory): RequestHistory {
    logger.info("saving request history for correlation id: ${request.correlationId}")
    val entity = requestHistoryRepository.save(request)
    return entity
  }

  override suspend fun getRequestHistoryById(id: UUID): RequestHistory {
    val requestHistory = requestHistoryRepository.findById(id)
    if (requestHistory == null) {
      throw NotFoundException("Request history with id: $id not found")
    }
    return requestHistory
  }

  override suspend fun deleteRequestHistoryById(id: UUID) {
    TODO("Not yet implemented")
  }
}
