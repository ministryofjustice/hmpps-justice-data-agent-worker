package uk.gov.justice.digital.hmpps.justicedataagentworker.utility

import com.fasterxml.uuid.Generators
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class DataGenerator {

  companion object {

    val jsonRequestSchema = "{\n" +
      "  \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
      "  \"title\": \"case-note-analysis\",\n" +
      "  \"type\": \"array\",\n" +
      "  \"items\": {\n" +
      "    \"type\": \"object\",\n" +
      "    \"title\": \"CaseNote\",\n" +
      "    \"properties\": {\n" +
      "      \"item_id\": {\n" +
      "        \"type\": \"string\",\n" +
      "        \"format\": \"uuid\",\n" +
      "        \"description\": \"Unique identifier for the case note entry\"\n" +
      "      },\n" +
      "      \"case_note_text\": {\n" +
      "        \"type\": \"string\",\n" +
      "        \"description\": \"Free-text content of the case note\"\n" +
      "      }\n" +
      "    },\n" +
      "    \"required\": [\"item_id\", \"case_note_text\"],\n" +
      "    \"additionalProperties\": false\n" +
      "  }\n" +
      "}"

    val jsonResponseSchema = "\n" +
      "\n" +
      "\n" +
      "{\n" +
      "  \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
      "  \"title\": \"BehaviourRiskAssessments\",\n" +
      "  \"type\": \"array\",\n" +
      "  \"items\": {\n" +
      "    \"type\": \"object\",\n" +
      "    \"properties\": {\n" +
      "      \"item_id\": { \"type\": \"integer\" },\n" +
      "      \"usual_behaviour_presentation\": {\n" +
      "        \"type\": \"integer\",\n" +
      "        \"minimum\": 0,\n" +
      "        \"maximum\": 5\n" +
      "      },\n" +
      "      \"risks_and_triggers\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"protective_factors\": { \"type\": \"integer\", \"minimum\": 0, \"maximum\": 5 },\n" +
      "      \"comment\": { \"type\": \"string\" },\n" +
      "      \"confidence_level\": {\n" +
      "        \"type\": \"string\",\n" +
      "        \"enum\": [\"low\", \"medium\", \"high\"]\n" +
      "      },\n" +
      "      \"justifying_spans\": {\n" +
      "        \"type\": \"array\",\n" +
      "        \"items\": {\n" +
      "          \"type\": \"object\",\n" +
      "          \"properties\": {\n" +
      "            \"text\": { \"type\": \"string\" },\n" +
      "            \"justifies\": {\n" +
      "              \"type\": \"string\",\n" +
      "              \"enum\": [\n" +
      "                \"usual_behaviour_presentation\",\n" +
      "                \"risks_and_triggers\",\n" +
      "                \"protective_factors\"\n" +
      "              ]\n" +
      "            }\n" +
      "          },\n" +
      "          \"required\": [\"text\", \"justifies\"],\n" +
      "          \"additionalProperties\": false\n" +
      "        }\n" +
      "      }\n" +
      "    },\n" +
      "    \"required\": [\n" +
      "      \"item_id\",\n" +
      "      \"usual_behaviour_presentation\",\n" +
      "      \"risks_and_triggers\",\n" +
      "      \"comment\",\n" +
      "      \"confidence_level\",\n" +
      "      \"justifying_spans\"\n" +
      "    ],\n" +
      "    \"additionalProperties\": false\n" +
      "  }\n" +
      "}"
    fun buildPrompt(promptVersion: MutableSet<PromptVersion>): Prompt = Prompt(
      Generators.timeBasedEpochGenerator().generate(),
      UUID.randomUUID().toString(),
      promptVersion,
      "Inline instruction  FOR LLM",
      false,
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
    )

    fun buildPromptVersion(prompt: Prompt, requestJsonSchema: String, responseJsonSchema: String): PromptVersion = PromptVersion(
      Generators.timeBasedEpochGenerator().generate(),
      1,
      prompt,
      "Inline instruction  FOR LLM",
      false,
      responseJsonSchema,
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
    )
  }
}
