package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class RequestType(private val value: String) {
  SYNC("sync"),
  ASYNC("async"),
  ;

  fun getValue(): String = value

  override fun toString(): String = value
}
