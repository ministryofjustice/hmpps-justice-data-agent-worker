package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.openai.models.chat.completions.ChatCompletion
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.LiteLlmService
import uk.gov.justice.digital.hmpps.justicedataagentworker.validator.JsonSchemaValidator

@Service
class JdaWorkerServiceImpl(
  private val jsonSchemaValidator: JsonSchemaValidator,
  private val liteLlmService: LiteLlmService,
  private val promptVersionService: PromptVersionService,
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
    /*if (response is ChatCompletion) {
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
    }*/
    if (!jsonSchema.isNullOrBlank()) {
      logger.info("Validating data with json schema: {}")
      jsonSchemaValidator.validateJson(jsonSchema!!, response as String)
    }
    return response!!
  }

  override suspend fun handleSynchronousRequest(jdaRequest: JdaRequest): JdaResponse {
    var promptVersionResponse: PromptVersionResponse? = null
    promptVersionService.getPromptVersionByKeyAndVersion(jdaRequest.prompt.key, jdaRequest.prompt.version)?.let { version ->
      promptVersionResponse = version
    }
    val aiResponse = liteLlmService.connect(
      convertMessageToPrompt(
        promptVersionResponse!!.promptTemplate,
        jdaRequest.requestData as String,
      ),
      promptVersionResponse!!.llmModel,
      false,
    )
    val response = convertAiResponseToApiResponse(aiResponse)
    return JdaResponse(
      jdaRequest.requestId,
      jdaRequest.correlationId,
      jdaRequest.prompt,
      response,
    )
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
