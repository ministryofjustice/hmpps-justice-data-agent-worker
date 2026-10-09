package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

data class PromptRequest(
  val promptKey: String,
  val description: String,
  val createdBy: String,
  val promptVersion: PromptVersionRequest,
)
