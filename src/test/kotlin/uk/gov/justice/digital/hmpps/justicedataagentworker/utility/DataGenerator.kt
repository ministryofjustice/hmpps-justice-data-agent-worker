package uk.gov.justice.digital.hmpps.justicedataagentworker.utility

import com.fasterxml.uuid.Generators
import io.r2dbc.postgresql.codec.Json
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class DataGenerator {

  companion object {

    val jsonRequestSchema = """
      {
        "${'$'}schema": "http://json-schema.org/draft-07/schema#",
        "title": "case-note-analysis",
        "type": "array",
        "items": {
          "type": "object",
          "title": "CaseNote",
          "properties": {
            "item_id": {
              "type": "string",
              "format": "uuid",
              "description": "Unique identifier for the case note entry"
            },
            "case_note_text": {
              "type": "string",
              "description": "Free-text content of the case note"
            }
          },
          "required": ["item_id", "case_note_text"],
          "additionalProperties": false
        }
      }
    """.trimIndent()

    val jsonResponseSchema = """
      {
        "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
        "title": "BehaviourRiskAssessments",
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "item_id": { "type": "integer" },
            "usual_behaviour_presentation": {
              "type": "integer",
              "minimum": 0,
              "maximum": 5
            },
            "risks_and_triggers": { "type": "integer", "minimum": 0, "maximum": 5 },
            "protective_factors": { "type": "integer", "minimum": 0, "maximum": 5 },
            "comment": { "type": "string" },
            "confidence_level": {
              "type": "string",
              "enum": ["low", "medium", "high"]
            },
            "justifying_spans": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "text": { "type": "string" },
                  "justifies": {
                    "type": "string",
                    "enum": [
                      "usual_behaviour_presentation",
                      "risks_and_triggers",
                      "protective_factors"
                    ]
                  }
                },
                "required": ["text", "justifies"],
                "additionalProperties": false
              }
            }
          },
          "required": [
            "item_id",
            "usual_behaviour_presentation",
            "risks_and_triggers",
            "comment",
            "confidence_level",
            "justifying_spans"
          ],
          "additionalProperties": false
        }
      }
    """.trimIndent()

    fun buildPrompt(promptVersion: MutableSet<PromptVersion>): Prompt = Prompt(
      Generators.timeBasedEpochGenerator().generate(),
      UUID.randomUUID().toString(),
      // promptVersion,
      "Inline instruction  FOR LLM",
      false,
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
    )

    fun buildPrompt(key: String, createdBy: UUID): Prompt = Prompt(
      Generators.timeBasedEpochGenerator().generate(),
      key,
      // promptVersion,
      "Inline instruction  FOR LLM",
      false,
      createdBy,
      LocalDateTime.now(ZoneOffset.UTC),
    )

    fun buildPromptVersion(promptId: UUID, requestJsonSchema: String, responseJsonSchema: String): PromptVersion = PromptVersion(
      Generators.timeBasedEpochGenerator().generate(),
      1,
      promptId,
      "Test-Model-x1",
      "Inline instruction  FOR LLM",
      Json.of(requestJsonSchema),
      Json.of(responseJsonSchema),
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
    )
  }
}
