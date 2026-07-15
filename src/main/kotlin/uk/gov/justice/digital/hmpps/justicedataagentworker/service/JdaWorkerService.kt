package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import org.springframework.ai.chat.prompt.Prompt

interface JdaWorkerService {
  fun analyzeData(prompt: Prompt, model: String, jsonSchema: String?, useWebClient: Boolean): Any
}
