package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.JdaWorkerService
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.ChatCompletionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.Message
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestController
class JdaResource(private val jdaWorkerService: JdaWorkerService) {

  // This endpoint is for test purpose only for developer

  @Operation(
    summary = "Synchronous request to jda worker.",
    description = "This api endpoint is for sending synchronous request  to jda worker.  Requires role ROLE_JUSTICE_DATA_AGENT_REQUESTS",
    security = [SecurityRequirement(name = "JUSTICE_DATA_AGENT_REQUESTS")],
    responses = [
      ApiResponse(responseCode = "200", description = "Successful response from LLM"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint. The issue can be logged staff and prisoner have different establishment.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @PostMapping("v1/chat/jda/worker", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  //@PreAuthorize("hasAnyRole('JUSTICE_DATA_AGENT_REQUESTS')")
  suspend fun analyze(
    @RequestBody jdaRequest: JdaRequest,
  ): ResponseEntity<JdaResponse> {
    val jdaResponse = jdaWorkerService.handleSynchronousRequest(jdaRequest)
    return ResponseEntity.status(HttpStatus.OK).body(jdaResponse)
  }

  @PostMapping("v1/chat/completion", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  suspend fun createSummary(
    @RequestBody contents: List<uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Content>,
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

  private fun buildLLMRequests(contents: List<uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.Content>, model: String): ChatCompletionRequest {
    var messages: MutableList<Message> = mutableListOf()
    contents.forEach { x ->
      messages.add(Message(x.role, x.content))
    }
    return ChatCompletionRequest(model, messages)
  }
}
