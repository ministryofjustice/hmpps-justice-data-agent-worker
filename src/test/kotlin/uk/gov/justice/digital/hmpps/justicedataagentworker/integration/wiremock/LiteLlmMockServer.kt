package uk.gov.justice.digital.hmpps.justicedataagentworker.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LiteLlmApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val liteLlm = LiteLlmMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    liteLlm.start()
    liteLlm.stubGrantChatCompletion()
  }

  override fun beforeEach(context: ExtensionContext) {
    liteLlm.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    liteLlm.stop()
  }
}

class LiteLlmMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8091
  }

  fun stubGrantChatCompletion() {
    stubFor(
      post(urlEqualTo("/chat/completions"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
                {
                    "id": "chatcmpl-d007c735-e552-4d5d-bf0a-c36f21dade84",
                    "created": 1784189820,
                    "model": "test-model-x",
                    "object": "chat.completion",
                    "choices": [
                        {
                            "finish_reason": "stop",
                            "index": 0,
                            "message": {
                                "content": "The capital of France is **Paris**.",
                                "role": "assistant"
                            }
                        }
                    ],
                    "usage": {
                        "completion_tokens": 11,
                        "prompt_tokens": 11,
                        "total_tokens": 22,
                        "completion_tokens_details": {
                            "reasoning_tokens": 0,
                            "text_tokens": 11
                        },
                        "prompt_tokens_details": {
                            "cached_tokens": 0,
                            "text_tokens": 11,
                            "cache_creation_tokens": 0
                        },
                        "cache_creation_input_tokens": 0,
                        "cache_read_input_tokens": 0
                    }
                }
              """.trimIndent(),
            ),
        ),
    )
  }
}
