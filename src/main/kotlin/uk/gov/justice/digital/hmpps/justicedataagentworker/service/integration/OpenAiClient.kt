package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class OpenAiClient(
  @param:Value("\${hmpps.ai-gateway.model.prefix}") private val modelPrefix: String,
  private val chatClient: ChatClient,
) {
  suspend fun getOpenAiChatResponse(prompt: Prompt, model: String): Any = chatClient.prompt(prompt)
    .options(OpenAiChatOptions.builder().model("$modelPrefix-$model").store(false))
    .call()
    .chatClientResponse()
}
