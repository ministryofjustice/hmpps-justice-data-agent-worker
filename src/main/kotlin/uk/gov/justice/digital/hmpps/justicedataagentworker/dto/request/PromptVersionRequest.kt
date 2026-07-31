package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

data class PromptVersionRequest(
  // val version: Int,
  val llmModel: String,
  val promptTemplate: String,
  val requestContract: String,
  val responseContract: String? = null,
)
