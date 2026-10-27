package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class JdaRequest(
  @Schema(description = "A reference value passed to JDA to for tracking", required = true, example = "39f262b1-a632-408a-9a71-7261225687af")
  val correlationId: UUID,
  val prompt: Prompt,
  @Schema(
    description = "Input data in array form to be sent to LLM.",
    required = true,
    example = """
      [
      {
      "item_id" : 1,
      "item_description": "Id 1 belongs to commerce department. This department has 20 members"
      },
      {
      "item_id" : 2,
      "item_description": "Id 1 belongs to sale department. This department has 5 members."
      }
      ]
    """,
  )
  var requestData: Any,
  // This field is for internal use only for async jda request only, to add request history id before putting jda request in queue
  @Schema(hidden = true)
  var requestId: UUID? = null,
)

data class Prompt(
  @Schema(description = "Key to get a prompt", required = true, example = "service-name-key")
  val key: String,
  @Schema(description = "Prompt version to be used. When no value is passed, latest version is always used.", required = false, example = "1")
  val version: Long? = null,
)
