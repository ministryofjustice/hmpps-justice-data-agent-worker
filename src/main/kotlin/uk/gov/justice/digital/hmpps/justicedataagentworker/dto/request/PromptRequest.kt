package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class PromptRequest(
  @Schema(description = "Key to get a prompt", required = true, example = "service-name-key")
  val promptKey: String,
  @Schema(description = "What this prompt is for and what is expected output from LLM", required = true, example = "This is prompt to get list of countries by country name and country code in json form sorted alphabetically.")
  val description: String,
  @Schema(description = "User id of the user who created the prompt.", required = true, example = "ABC@GMAIL.COM or USERID123")
  val createdBy: String,
  val promptVersion: PromptVersionRequest,
)
