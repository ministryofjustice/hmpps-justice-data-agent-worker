package uk.gov.justice.digital.hmpps.justicedataagentworker.service

import org.springframework.ai.chat.prompt.Prompt
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.JdaRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.JdaResponse

interface JdaWorkerService {
  suspend fun analyzeData(prompt: Prompt, model: String, jsonSchema: String?, useWebClient: Boolean): Any

  suspend fun handleSynchronousRequest(jdaRequest: JdaRequest): JdaResponse

  suspend fun handleAsynchronousRequest(jdaRequest: JdaRequest)

  suspend fun submitAsynchronousRequest(jdaRequest: JdaRequest)

  suspend fun dequeueResponse(): JdaResponse
}
