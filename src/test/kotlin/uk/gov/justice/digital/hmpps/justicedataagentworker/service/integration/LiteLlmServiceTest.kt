package uk.gov.justice.digital.hmpps.justicedataagentworker.service.integration

import com.openai.models.chat.completions.ChatCompletion
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock.LiteLlmApiExtension.Companion.liteLlm

class LiteLlmServiceTest : IntegrationTestBase() {

  @Autowired
  private lateinit var service: LiteLlmService

  @BeforeEach
  fun setUp() {
    liteLlm.start()
    liteLlm.stubChatCompletion()
  }

  @AfterEach
  fun tearDown() {
    liteLlm.stop()
  }

  @Test
  fun `test lite Llm service for rest client`() {
    runBlocking {
      val prompt = Prompt(UserMessage("Where is capital of France?"))
      val res = service.connect(prompt, "test-model-x", true)
      assertInstanceOf(ChatCompletion::class.java, res)
    }
  }

  @Test
  fun `test lite Llm service for open ai client`() {
    runBlocking {
      val prompt = Prompt(UserMessage("Where is capital of France?"))
      val res = service.connect(prompt, "test-model-x", false)
      assertInstanceOf(ChatClientResponse::class.java, res)
    }
  }
}
