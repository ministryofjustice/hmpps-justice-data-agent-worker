package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Prompt
import java.util.UUID

data class JdaResponse(
  val requestId: UUID,
  val correlationId: UUID,
  val prompt: Prompt,
  val status: Status,
  val responseData: Any,
  val metaData: MetaData,
)
