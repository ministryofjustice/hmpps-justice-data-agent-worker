package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import java.util.UUID

data class JdaRequest(
  val correlationId: UUID,
  val prompt: Prompt,
  val requestData: Any,
)

data class Prompt(
  val key: String,
  val version: Int,
)
