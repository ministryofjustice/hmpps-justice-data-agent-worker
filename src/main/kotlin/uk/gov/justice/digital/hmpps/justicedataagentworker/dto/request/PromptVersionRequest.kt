package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import java.util.UUID

data class PromptVersionRequest(
  val version: Int,
  val promptId: UUID,
  val llmModel: String,
  val promptTemplate: String,
  val requestContract: String,
  val responseContract: String? = null,
  val createdBy: UUID,
)
