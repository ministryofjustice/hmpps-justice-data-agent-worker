package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse

interface JdaMessagePublisher {

  fun publishJdaResponse(jdaResponse: JdaResponse)
}
