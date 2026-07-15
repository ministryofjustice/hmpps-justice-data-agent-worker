package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Content
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.JdaWorkerService
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.ChatCompletionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.Message

@RestController
class JdaResource(private val jdaWorkerService: JdaWorkerService) {

  // This endpoint is for test purpose only
  @PostMapping("v1/chat/completion", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  fun createSummary(
    @RequestBody contents: List<Content>,
    @RequestParam(required = true) verifyOutputSchema: Boolean,
    @RequestParam(required = true) useWebClient: Boolean,
    @RequestParam(required = true) model: String,
  ): ResponseEntity<Any> {
    val req = buildLLMRequests(contents, model)
    val list = mutableListOf<org.springframework.ai.chat.messages.Message>()
    req.messages.forEach { msg ->
      if (msg.role == "system") {
        list.add(SystemMessage(msg.content))
      } else if (msg.role == "user") {
        list.add(UserMessage(msg.content))
      } else {
        throw RuntimeException("Invalid role: ${msg.role}")
      }
    }
    var jsonSchema: String? = null
    if (verifyOutputSchema) {
      jsonSchema = "\n" +
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
    }
    val response = jdaWorkerService.analyzeData(Prompt(list), model, jsonSchema, useWebClient)
    return ResponseEntity.status(HttpStatus.OK).body(response)
  }

  private fun buildLLMRequests(contents: List<Content>, model: String): ChatCompletionRequest {
    var messages: MutableList<Message> = mutableListOf()
    contents.forEach { x ->
      messages.add(Message(x.role, x.content))
    }
    return ChatCompletionRequest(model, messages)
  }
}
