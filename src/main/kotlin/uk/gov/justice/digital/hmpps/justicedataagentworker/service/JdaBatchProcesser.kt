package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest

interface JdaBatchProcesser {

  suspend fun processJdaRequest(jdaRequest: JdaRequest, batchSize: Int): List<JdaRequest>
}
