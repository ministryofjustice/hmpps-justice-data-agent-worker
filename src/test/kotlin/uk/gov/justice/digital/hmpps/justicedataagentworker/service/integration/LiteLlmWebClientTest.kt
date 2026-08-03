package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.LiteLlmApiExtension.Companion.liteLlm

class LiteLlmWebClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var liteLlmWebClient: LiteLlmWebClient

  @BeforeEach
  fun setUp() {
   // liteLlm.start()
   // liteLlm.stubGrantChatCompletion()
  }

  @AfterEach
  fun tearDown() {
   // liteLlm.stop()
  }

  @Test
  fun `send request to lite LLM api`() {
    val response = liteLlmWebClient.liteLlmChatCompletion(
      ChatCompletionRequest(
        model = "Random-test-model",
        listOf<Message>(Message("user", "Where is capital of france?")),
      ),
    ) as Map<String, Any>
    Assertions.assertTrue { response.containsKey("id") }
    Assertions.assertTrue { response.containsKey("model") }
    Assertions.assertTrue { response.containsKey("choices") }
  }
}
