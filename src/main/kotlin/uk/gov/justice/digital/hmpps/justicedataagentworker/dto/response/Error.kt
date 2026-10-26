package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class Error(
  @Schema(description = "Error code", required = true, example = "LLM_REFUSED_REQUEST or RESPONSE_CONTRACT_VALIDATION_FAILED")
  val code: String,
  @Schema(description = "Error message", required = true, example = "Validation failed. item_id do not exist in response.")
  val message: String,
  @Schema(description = "Stage to indicate success or failure.", required = true, example = "internal or gateway or skill or validation.")
  val stage: Stage,
)
