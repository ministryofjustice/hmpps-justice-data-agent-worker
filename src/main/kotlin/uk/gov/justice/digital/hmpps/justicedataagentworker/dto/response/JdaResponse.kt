package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Prompt
import java.util.UUID

data class JdaResponse(
  @Schema(description = "Request id to track request history.", required = true, example = "39f262b1-a632-408a-9a71-7261225687af")
  val requestId: UUID,
  @Schema(description = "Correlation id is provided by calling service to track in calling service.", required = true, example = "39f262b1-a632-408a-9a71-7261225687af")
  val correlationId: UUID,
  val prompt: Prompt,
  @Schema(description = "Status indicate if LLM processing succeeded or failed or rejected", required = true, example = "succeeded")
  val status: Status,
  // Remove this when services update to get receipt if from metaData.
  var receiptId: String?,
  @Schema(description = "The expected response data from LLM. Response can be data in an array or without array.", required = true,
    example = """
      [
      {
      "name": "United Kingdom",
      "code": "+44"
      }
      ]
    """)
  val responseData: Any,
  val metaData: MetaData,
)
