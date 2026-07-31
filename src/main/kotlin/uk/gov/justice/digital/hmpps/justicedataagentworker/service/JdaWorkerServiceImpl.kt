package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.fasterxml.uuid.Generators
import com.openai.models.chat.completions.ChatCompletion
import io.swagger.v3.core.util.Json
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.LiteLlmException
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.RequestHistory
import uk.gov.justice.digital.hmpps.justicedataagentworker.model.Status
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.LiteLlmService
import uk.gov.justice.digital.hmpps.justicedataagentworker.validator.JsonSchemaValidator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class JdaWorkerServiceImpl(
  private val jsonSchemaValidator: JsonSchemaValidator,
  private val liteLlmService: LiteLlmService,
  private val promptService: PromptService,
  private val requestHistoryService: RequestHistoryService,
) : JdaWorkerService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override suspend fun analyzeData(
    prompt: Prompt,
    model: String,
    jsonSchema: String?,
    useWebClient: Boolean,
  ): Any {
    var response = liteLlmService.connect(prompt, model, useWebClient)
    response = convertAiResponseToApiResponse(response)
    if (!jsonSchema.isNullOrBlank()) {
      logger.info("Validating data with json schema: {}")
      jsonSchemaValidator.validateJson(jsonSchema!!, response as String)
    }
    return response
  }

  override suspend fun handleSynchronousRequest(jdaRequest: JdaRequest): JdaResponse {
    var promptVersionResponse = promptService.getPromptsByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version)
    val time = LocalDateTime.now(ZoneOffset.UTC)
    var requestHistory: RequestHistory? = null
    var aiResponse: Any? = null
    var inputJson = jdaRequest.requestData
    inputJson = Json.pretty(inputJson)
    jsonSchemaValidator.validateJson(promptVersionResponse.promptVersion .requestContract, inputJson)
    requestHistory = RequestHistory(
      Generators.timeBasedEpochGenerator().generate(),
      true,
      jdaRequest.correlationId,
      promptVersionResponse.id!!,
      time,
      time,
      null,
      Status.QUEUED,
      null,
      null,
    )
    coroutineScope {
      launch {
        requestHistoryService.saveRequestHistory(requestHistory)
      }
      launch {
        aiResponse = liteLlmService.connect(
          convertMessageToPrompt(
            promptVersionResponse.promptVersion.promptTemplate,
            inputJson,
          ),
          promptVersionResponse.promptVersion.llmModel,
          false,
        )
      }
    }
    try {
      val response = convertAiResponseToApiResponse(aiResponse!!)
      if (promptVersionResponse.promptVersion.responseContract != null) {
        jsonSchemaValidator.validateJson(promptVersionResponse.promptVersion.responseContract, response as String)
      }
      // requestHistory = requestHistoryService.getRequestHistoryById(requestHistory.id!!)
      requestHistory?.new = false
      requestHistory?.status = Status.SUCCEEDED
      requestHistory?.completedAt = LocalDateTime.now(ZoneOffset.UTC)
      requestHistoryService.saveRequestHistory(requestHistory!!)
      return JdaResponse(
        UUID.randomUUID(),
        jdaRequest.correlationId,
        jdaRequest.prompt,
        ObjectMapper().readTree(response as String),
      )
    } catch (e: Exception) {
      logger.error("Error processing connecting to lite llm: {}", e)
      // requestHistory = requestHistoryService.getRequestHistoryById(requestHistory.id!!)
      requestHistory?.new = false
      requestHistory?.status = Status.REJECTED
      requestHistory?.errorAt = LocalDateTime.now(ZoneOffset.UTC)
      requestHistory?.errorMessage = e.message
      requestHistoryService.saveRequestHistory(requestHistory!!)
      throw LiteLlmException("Exception getting llm response: e")
    }
  }

  override suspend fun handleAsynchronousRequest(jdaRequest: JdaRequest): JdaResponse {
    TODO("Not yet implemented")
  }

  private fun convertMessageToPrompt(systemMessage: String, userMessage: String): Prompt {
    val list = mutableListOf<org.springframework.ai.chat.messages.Message>()
    list.add(SystemMessage(systemMessage))
    list.add(UserMessage(userMessage))
    return Prompt(list)
  }

  private fun convertAiResponseToApiResponse(aiResponse: Any): Any {
    var response = aiResponse
    if (response is ChatCompletion) {
      val message = response.choices().get(0) as Map<String, Any>
      val content = message["message"] as Map<String, Any>
      response = content["content"] as String
      response = response.replace("```json", "")
      response = response.replace("```", "")
      response = response.replace("\n", "")
    }

    if (response is ChatClientResponse) {
      response = response.chatResponse!!.result!!.output.text!!
      response = response.replace("```json", "")
      response = response.replace("```", "")
      response = response.replace("\n", "")
    }
    return response
  }
}
