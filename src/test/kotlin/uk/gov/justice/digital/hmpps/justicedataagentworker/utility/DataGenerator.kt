package uk.gov.justice.digital.hmpps.justicedataagentworker.utility

import com.fasterxml.uuid.Generators
import io.r2dbc.postgresql.codec.Json
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.PromptVersion
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class DataGenerator {

  companion object {

    val jsonRequestSchema = $$"""
      {
        "$schema": "http://json-schema.org/draft-07/schema#",
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
    """.replace("\n", "").replace("  ", " ").trimIndent()

    val jsonResponseSchema = """
      {
        "'$'schema": "https://json-schema.org/draft/2020-12/schema",
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
    """.replace("\n", "").replace("  ", " ").trimIndent()

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

    fun buildLlmResponse() = """
      {
          "id": "chatcmpl-8177f937-cc5d-424b-ba14-da717d661942",
          "created": 1787086868,
          "model": "bedrock-claude-sonnet-4-5",
          "object": "chat.completion",
          "choices": [
              {
                  "finish_reason": "stop",
                  "index": 0,
                  "message": {
                      "content": "```json\n{\n  \"topic\": \"History of Boston\",\n  \"summary\": \"Boston is one of the oldest cities in the United States with a rich historical significance.\",\n  \"key_periods\": [\n    {\n      \"period\": \"Founding (1630)\",\n      \"description\": \"Boston was founded by Puritan colonists from England led by John Winthrop. It was named after Boston in Lincolnshire, England.\"\n    },\n    {\n      \"period\": \"Colonial Era (1630-1776)\",\n      \"description\": \"Boston became a major commercial and shipping center. It was the largest town in British America until Philadelphia grew larger in the mid-18th century.\"\n    },\n    {\n      \"period\": \"American Revolution (1760s-1780s)\",\n      \"description\": \"Boston was a center of revolutionary activity, including the Boston Massacre (1770), Boston Tea Party (1773), and the beginning of the Revolutionary War with the Battles of Lexington and Concord (1775).\"\n    },\n    {\n      \"period\": \"19th Century\",\n      \"description\": \"Boston emerged as a cultural and intellectual center, became a major manufacturing hub, and experienced significant immigration, particularly from Ireland.\"\n    },\n    {\n      \"period\": \"20th Century to Present\",\n      \"description\": \"Boston transformed into a center for education, medicine, technology, and finance. Home to numerous universities including Harvard and MIT.\"\n    }\n  ],\n  \"notable_events\": [\n    \"Boston Massacre (1770)\",\n    \"Boston Tea Party (1773)\",\n    \"Battle of Bunker Hill (1775)\",\n    \"Great Boston Fire (1872)\",\n    \"Boston Police Strike (1919)\",\n    \"Busing Crisis (1970s)\"\n  ]\n}\n```",
                      "role": "assistant"
                  }
              }
          ],
          "usage": {
              "completion_tokens": 414,
              "prompt_tokens": 27,
              "total_tokens": 441,
              "completion_tokens_details": {
                  "reasoning_tokens": 0,
                  "text_tokens": 414
              },
              "prompt_tokens_details": {
                  "cached_tokens": 0,
                  "text_tokens": 27,
                  "cache_creation_tokens": 0
              },
              "cache_creation_input_tokens": 0,
              "cache_read_input_tokens": 0
          }
      }
    """.trimIndent()

    fun buildJdaRequest(correlationId: UUID, promptKey: String, version: Int): JdaRequest = JdaRequest(
      correlationId,
      uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Prompt(promptKey, version),
      ObjectMapper().readTree(
        """
          [
            {
              "item_id": "76304207-b018-4812-a3bf-f294a05347e8",
              "case_note_text": "New Induction  - Test User arrived at Test Prison on 14/03/2025, this is his third time in custody and he appeared visibly anxious and withdrawn upon arrival. He stated he believed he should be on an enhanced CSRA level but has been placed on standard. Test User expressed a desire to transfer to a facility closer to his hometown so that his partner and children could visit more regularly. He disclosed a previous history of self-harm but stated he has no current thoughts or intentions. Test User mentioned he has some historic links to a local group but was unable to identify any known conflicts within this establishment. Test User confirmed he understood the support options available to him and said he would approach staff if he needed assistance. He was polite throughout the conversation and expressed gratitude at the end of the session."
            },
            {
              "item_id": "33787111-c923-4e5e-ad92-eba8f2e122e8",
              "case_note_text": "During this evening's welfare check, Test Prisoner was in a very distressed state and was unable to engage in any meaningful conversation regarding his wellbeing or future plans. He was unable to confirm whether he had any immediate intentions to harm himself, and when asked directly whether he could keep himself safe overnight, he gave no clear assurance. Staff discussed the possibility of increased monitoring with the on-call manager. Following a brief MDT consultation via telephone, it was agreed that Marcus should be placed on a two-person watch for the remainder of the night, with a formal review to take place at morning handover. Test Prisoner was informed of this decision and did not object."
            },
            {
              "item_id": "ba5c440b-31b8-411d-bbb8-139576e4b5ad",
              "case_note_text": "Test Prisoner  was relocated to the Separation and Care Unit earlier today following concerns raised overnight. He declined all activities offered during the morning regime including exercise and association."
            },
            {
              "item_id": "33ac889f-8bab-4a39-9cf3-28e9d9c8ad95",
              "case_note_text": "Prior to meeting with Mr. Other, I reviewed his recent case notes to prepare for our first key work session together. I noted he has received two warnings in recent weeks - one related to an altercation with another resident and one relating to possession of unauthorised items. He is currently without employment and his ACCT was opened recently following a self-harm disclosure. I met with Mr. Other in his cell and introduced myself as his allocated keyworker and explained the purpose of the session. He initially said everything was fine and seemed reluctant to engage, however after I clarified what key work involves he agreed to participate. His cell was in a poor state of cleanliness. I asked him about the recent warnings and he explained that tensions had arisen with another resident over a misunderstanding, and that the second incident was due to taking items he felt were owed to him. I encouraged him to approach staff in future rather than taking matters into his own hands. ACTION PLAN: 1. Reapply for employment on the wing. 2. Improve cell cleanliness."
            },
            {
              "item_id": "85a964c3-875b-4ff4-a54a-a528ce03454a",
              "case_note_text": "On 22/05/2025 at approximately 11:30, Mr. Other approached me near the servery on B wing and requested to speak about a personal property matter. He stated that several items of clothing had gone missing following a cell search carried out the previous week. I explained that I was not present during that search but that I would look into the matter and follow up with him. Mr. Other became increasingly agitated and began raising his voice, accusing staff of stealing from him. He used threatening and offensive language toward me directly. I activated my body-worn camera and asked him calmly to return to his cell while the matter was investigated. Mr. Other became physically confrontational. A colleague responded to my request for assistance and together we guided Mr. Other back to his cell using appropriate restraint techniques. He was secured in his cell without further incident. Mr. Other will be placed on report for threatening behaviour and failure to comply with a lawful instruction."
            }
          ]
        """.trimIndent(),
      ),
    )
  }
}
