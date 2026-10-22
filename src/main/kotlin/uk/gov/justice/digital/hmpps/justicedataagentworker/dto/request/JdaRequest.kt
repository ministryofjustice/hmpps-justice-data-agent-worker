package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class JdaRequest(
  val correlationId: UUID,
  val prompt: Prompt,
  var requestData: Any,
  // This field is for internal use only for async jda request only, to add request history id before putting jda request in queue
  @Schema(hidden = true)
  var requestId: UUID? = null,
)

data class Prompt(
  val key: String,
  val version: Long? = null,
)
