package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response

import tools.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.UUID

data class PromptVersionResponse(
  var id: UUID,
  val version: Int,
  val llmModel: String,
  val promptTemplate: String,
  val batchSize: Int,
  val batchArrayName: String,
  val requestContract: JsonNode,
  val responseContract: JsonNode? = null,
  val createdBy: String,
  val createdDate: LocalDateTime,
)
