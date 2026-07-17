package uk.gov.justice.digital.hmpps.justicedataagentworker.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfiguration(
  @param:Value("\${hmpps.ai-gateway.api-key}") val apiKey: String,
  @param:Value("\${hmpps.ai-gateway.base-url}") val liteLlmBaseUrl: String,
) {

  @Bean
  fun hmppsOpenAiChatClient(): ChatClient {
    val model = OpenAiChatModel.builder()
      .options(
        OpenAiChatOptions.builder()
          .apiKey(apiKey)
          .baseUrl(liteLlmBaseUrl)
          .build(),
      ).build()
    val chatClient = ChatClient.builder(model).build()
    return chatClient
  }
}
