package uk.gov.justice.digital.hmpps.justicedataagentworker.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.request.PromptVersionRequest
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.dto.response.PromptVersionResponse
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.PromptService
import uk.gov.justice.digital.hmpps.justicedataagentworker.service.PromptVersionService
import java.util.UUID

@RestController
class PromptResource(
  private val promptService: PromptService,
  private val promptVersionService: PromptVersionService,
) {

  @PostMapping("/prompts")
  @PreAuthorize("hasAnyRole('JUSTICE_DATA_AGENT_PROMPTS')")
  suspend fun createPrompt(@RequestBody promptRequest: PromptRequest): ResponseEntity<PromptResponse> {
    val prompt = promptService.savePrompt(promptRequest)
    return ResponseEntity.status(HttpStatus.CREATED).body(prompt)
  }

  @GetMapping("/prompts/{id}")
  @PreAuthorize("hasAnyRole('JUSTICE_DATA_AGENT_PROMPTS')")
  suspend fun getPromptById(@PathVariable id: UUID): ResponseEntity<PromptResponse> {
    val prompt = promptService.getPromptById(id)
    return ResponseEntity.status(HttpStatus.CREATED).body(prompt)
  }

  @PostMapping("/promptversions")
  @PreAuthorize("hasAnyRole('JUSTICE_DATA_AGENT_PROMPTS')")
  suspend fun createPromptVersion(@RequestBody promptVersionRequest: PromptVersionRequest): ResponseEntity<PromptVersionResponse> {
    val promptVersion = promptVersionService.savePromptVersion(promptVersionRequest)
    return ResponseEntity.status(HttpStatus.CREATED).body(promptVersion)
  }

  @GetMapping("/promptversions/{id}")
  @PreAuthorize("hasAnyRole('JUSTICE_DATA_AGENT_PROMPTS')")
  suspend fun getPromptVersionById(@PathVariable id: UUID): ResponseEntity<PromptVersionResponse> {
    val promptVersion = promptVersionService.getPromptVersionById(id)
    return ResponseEntity.status(HttpStatus.CREATED).body(promptVersion)
  }
}
