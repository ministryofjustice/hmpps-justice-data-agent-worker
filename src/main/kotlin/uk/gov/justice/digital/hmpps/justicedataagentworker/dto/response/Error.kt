package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

data class Error(
  val code: String,
  val message: String,
  val stage: Stage,
)
