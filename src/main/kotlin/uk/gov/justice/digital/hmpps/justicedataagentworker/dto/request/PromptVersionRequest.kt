package uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import tools.jackson.databind.JsonNode

data class PromptVersionRequest(
  @Schema(description = "Name of the model without any prefix added", required = true, example = "claud-4-6")
  val llmModel: String,
  @Schema(description = "Prompt to be send to LLM.", required = true, example = "Give me list of countries with name and countryCode as json field, sorted alphabetically by country name.")
  val promptTemplate: String,
  @Schema(description = "Max number of data in an array sent to LLM.", required = true, example = "5 or 10 or 20.")
  val batchSize: Int,
  @Schema(description = "Name of array.", required = true, example = "service-AA-array-size")
  val batchArrayName: String,
  @Schema(
    description = "Json schema for data sent to LLM",
    required = true,
    example = """
      {
  "${'$'}schema": "//w3schools.in/json-06/schema#",
  "title": "Professor",
  "type": "object",
  "properties": {
    "first_name": {
      "type": "string"
    },
    "last_name": {
      "type": "string"
    },
    "subject_teach": {
      "description": "Howmany_subjects",
      "type": "integer",
      "minimum": 1
    }
  },
  "required": [
    "first_name",
    "last_name"
  ]
}
    """,
  )
  val requestContract: JsonNode,
  @Schema(
    description = "Json schema for data expected from LLM.",
    required = false,
    example = """
      {
  "${'$'}schema": "//w3schools.in/json-06/schema#",
  "title": "Professor",
  "type": "object",
  "properties": {
    "first_name": {
      "type": "string"
    },
    "last_name": {
      "type": "string"
    },
    "subject_teach": {
      "description": "Howmany_subjects",
      "type": "integer",
      "minimum": 1
    }
  },
  "required": [
    "first_name",
    "last_name"
  ]
}
    """,
  )
  val responseContract: JsonNode? = null,
)
