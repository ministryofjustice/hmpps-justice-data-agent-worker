package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse

interface JdaMessagePublisher {

  suspend fun publishJdaRequest(jdaRequest: JdaRequest)

  suspend fun publishJdaResponse(jdaResponse: JdaResponse)
}
