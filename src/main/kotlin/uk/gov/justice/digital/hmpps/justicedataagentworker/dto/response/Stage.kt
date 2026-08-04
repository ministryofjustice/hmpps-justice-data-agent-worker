package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

enum class Stage(value: String) {
  INTERNAL("internal"),
  GATEWAY("gateway"),
  SKILL("skill"),
  VALIDATION("validation"),
}
