package uk.gov.justice.digital.hmpps.justicedataagentworker.service.event

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest

interface JdaMessageListener {

  fun onJdaRequestMessageReceived(message: JdaRequest)
}
