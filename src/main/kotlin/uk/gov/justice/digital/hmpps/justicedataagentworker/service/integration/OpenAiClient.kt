package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class OpenAiClient(
  private val chatClient: ChatClient,
) {
  fun getOpenAiChatResponse(prompt: Prompt, model: String): Any = chatClient.prompt(prompt)
    .options(OpenAiChatOptions.builder().model(model).store(false))
    .call()
    .chatClientResponse()
}
