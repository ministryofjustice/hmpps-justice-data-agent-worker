package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(Include.NON_NULL)
data class MetaData(
  @Schema(description = "Request type either sync or async", required = true, example = "async")
  val requestType: RequestType,
  @Schema(description = "Receipt id of the message in sqs", required = true, example = "39f262b1-a632-408a-9a71-7261225687af")
  var receiptId: String?,
  @Schema(description = "Time in GMT when request was received by JDA worker.", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val submittedAt: LocalDateTime?,
  @Schema(description = "Time in GMT when request was added to request queue.", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val queuedAt: LocalDateTime?,
  @Schema(description = "Time in GMT when response received from LLM or put to response queue.", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val processedAt: LocalDateTime?,
  @Schema(description = "Time in GMT when request received by JDA worker.", required = true)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val receivedAt: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(description = "Time in GMT when request processing completed", required = true)
  val completedAt: LocalDateTime?,
  @Schema(description = "Time in millisecond when request completed - request received.", required = true, example = "3500")
  val completionMs: Long?,
)
