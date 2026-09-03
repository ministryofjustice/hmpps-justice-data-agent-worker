package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class Status(private val value: String) {
  QUEUED("queued"),
  PROCESSING("processing"),
  SUCCEEDED("succeeded"),
  FAILED("failed"),
  REJECTED("rejected"), ;

  fun getValue(): String = value

  override fun toString(): String = value
}
