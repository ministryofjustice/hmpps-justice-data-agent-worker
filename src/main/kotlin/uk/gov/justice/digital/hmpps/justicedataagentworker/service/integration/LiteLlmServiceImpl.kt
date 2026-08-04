package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import com.openai.core.JsonField
import com.openai.core.JsonValue
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.completions.CompletionUsage
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.justicedataagentworker.exception.LiteLlmException

@Component
class LiteLlmServiceImpl(
  private val liteLlmWebClient: LiteLlmWebClient,
  private val openAiClient: OpenAiClient,
) : LiteLlmService {

  companion object {
    private val logger = LoggerFactory.getLogger(this::class.java)
  }
  override suspend fun connect(
    prompt: Prompt,
    model: String,
    useWebClient: Boolean,
  ): Any {
    var response: Any? = null
    if (useWebClient) {
      logger.info("Connecting to lite LLM using web client")
      val chatCompletionRequest = buildChatCompletionRequest(prompt, model)
      response = liteLlmWebClient.liteLlmChatCompletion(chatCompletionRequest)
      response = buildChatCompletion(response)
    } else {
      logger.info("Connecting to lite LLM using open ai client")
      response = openAiClient.getOpenAiChatResponse(prompt, model)
    }
    logger.info("returning lite LLM response")
    return response!!
  }

  private fun buildChatCompletion(response: Map<String, Any>): ChatCompletion {
    try {
      val usage = response!!["usage"] as Map<String, String>
      val completionUsage = CompletionUsage.builder()
        .totalTokens((usage["total_tokens"] as Int).toLong())
        .completionTokens((usage["completion_tokens"] as Int).toLong())
        .promptTokens((usage["prompt_tokens"] as Int).toLong())
        .build()
      return ChatCompletion.builder()
        .id(JsonField.of(response!!["id"] as String))
        .model(JsonField.of(response!!["model"] as String))
        .choices(JsonField.of(response!!["choices"] as List<ChatCompletion.Choice>))
        .created(JsonField.of((response!!["created"] as Int).toLong()))
        .usage(JsonField.of(completionUsage))
        .object_(JsonValue.from(JsonField.of(response!!["object"] as String)))
        .build()
    } catch (e: Exception) {
      throw LiteLlmException("Exception building chat completion from webclient response data ${e.message}")
    }
  }

  private fun buildChatCompletionRequest(prompt: Prompt, model: String): ChatCompletionRequest {
    val messages = mutableListOf<Message>()
    prompt.userMessages.forEach { userMessage ->
      messages.add(Message(userMessage.messageType.value, userMessage.text.toString()))
    }

    prompt.systemMessages.forEach { systemMessage ->
      messages.add(Message(systemMessage.messageType.value, systemMessage.text.toString()))
    }
    return ChatCompletionRequest(model, messages)
  }
}

data class ChatCompletionRequest(
  val model: String,
  val messages: List<Message>,
)

data class Message(
  val role: String,
  val content: String,
)
