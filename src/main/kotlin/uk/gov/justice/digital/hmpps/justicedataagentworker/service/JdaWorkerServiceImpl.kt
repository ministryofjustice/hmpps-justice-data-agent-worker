package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import com.openai.models.chat.completions.ChatCompletion
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration.LiteLlmService
import uk.gov.justice.digital.hmpps.justicedataagentworker.validator.JsonSchemaValidator

@Service
class JdaWorkerServiceImpl(
  private val jsonSchemaValidator: JsonSchemaValidator,
  private val liteLlmService: LiteLlmService,
) : JdaWorkerService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun analyzeData(
    prompt: Prompt,
    model: String,
    jsonSchema: String?,
    useWebClient: Boolean,
  ): Any {
    var response = liteLlmService.connect(prompt, model, useWebClient)

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
    if (!jsonSchema.isNullOrBlank()) {
      logger.info("Validating data with json schema: {}")
      jsonSchemaValidator.validateJson(jsonSchema!!, response as String)
    }
    return response!!
  }
}
