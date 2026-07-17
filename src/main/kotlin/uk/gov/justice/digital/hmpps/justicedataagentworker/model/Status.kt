package uk.gov.justice.digital.hmpps.justicedataagentworker.model

enum class Status(value: String) {
  QUEUED("queued"),
  PROCESSING("processing"),
  SUCCEEDED("succeeded"),
  FAILED("failed"),
  REJECTED("rejected"),
}