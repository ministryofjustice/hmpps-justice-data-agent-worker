package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import org.springframework.ai.chat.prompt.Prompt

interface LiteLlmService {
  suspend fun connect(prompt: Prompt, model: String, useWebClient: Boolean): Any
}
